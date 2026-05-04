"""PySpigot script loader.

Drives execution of PySpigot scripts that share a single embedded JEP
interpreter. Each script runs in its own ``globals`` dict so module-level
names defined by one script cannot leak into another. Imported modules
(``sys.modules``) and any state held inside them are shared, mirroring the
behaviour of an ordinary Python process.

The Java side (``ScriptManager``) is responsible for all bookkeeping around
script identity, file discovery, options, dependencies, and platform
integration. This module is intentionally narrow: it compiles + executes a
script's source, tracks per-script globals, and runs ``@start`` / ``@stop``
hooks declared by the script.
"""

import builtins
import linecache
import sys
import traceback
from os import fspath


_states = {}


class _ScriptState:
    """Per-script state held while a script is loaded.

    Held off-stack so the Java side never needs to round-trip through the
    Python globals dict to reach the hooks or project path.
    """

    __slots__ = (
        "globals",
        "main_path",
        "project_path",
        "java_script",
        "start_hooks",
        "stop_hooks",
    )

    def __init__(self, main_path, project_path, java_script, java_logger):
        self.globals = {
            "__name__": "__main__",
            "__file__": main_path,
            "__builtins__": builtins.__dict__,
            "__package__": None,
            "__loader__": None,
            "__spec__": None,
            "__doc__": None,
            "logger": java_logger,
        }
        self.main_path = main_path
        self.project_path = project_path
        self.java_script = java_script
        self.start_hooks = []
        self.stop_hooks = []


def load(script_id, java_script, java_logger, main_path, source, project_path=None):
    """Compile and execute a script's main source, then invoke any ``@start``
    hooks declared by it.

    Any exception raised during compilation, top-level execution, or a start
    hook propagates out so the Java side can surface it. On failure all
    partial state (sys.path entry, linecache entry, registry entry) is rolled
    back.

    Args:
        script_id: Stable lowercase identifier matched on later calls.
        java_script: The Java ``Script`` instance, passed to start/stop
            hooks that declare a single positional argument (matches the
            old Jython behaviour).
        java_logger: The script's logger; bound to ``logger`` in globals.
        main_path: Absolute path of the main ``.py`` file (used for
            tracebacks and ``__file__``).
        source: Main script source text.
        project_path: For multi-file projects, the absolute path of the
            project folder. Inserted at ``sys.path[0]`` so intra-project
            imports resolve.
    """
    if script_id in _states:
        raise RuntimeError("script already loaded: " + script_id)

    main_path = fspath(main_path)
    project_path = fspath(project_path) if project_path is not None else None

    state = _ScriptState(main_path, project_path, java_script, java_logger)

    inserted_path = False
    if project_path is not None and project_path not in sys.path:
        sys.path.insert(0, project_path)
        inserted_path = True

    linecache.cache[main_path] = (
        len(source),
        None,
        [line + "\n" for line in source.splitlines()],
        main_path,
    )

    try:
        code = compile(source, main_path, "exec")
        exec(code, state.globals)
    except BaseException:
        _rollback(state, inserted_path)
        raise

    _collect_hooks(state)
    _states[script_id] = state

    try:
        for hook in state.start_hooks:
            _invoke_hook(hook, java_script)
    except BaseException:
        del _states[script_id]
        _rollback(state, inserted_path)
        raise


def stop(script_id):
    """Invoke ``@stop`` hooks for the given script.

    Returns a list of ``(hook_name, formatted_traceback)`` tuples for any
    hooks that raised; an empty list means a clean stop. State is NOT
    cleared here — the Java side calls :func:`unload` afterwards once
    platform-side cleanup has run.
    """
    state = _states.get(script_id)
    if state is None:
        return []

    failures = []
    for hook in state.stop_hooks:
        try:
            _invoke_hook(hook, state.java_script)
        except BaseException:
            failures.append(
                (getattr(hook, "__name__", repr(hook)), traceback.format_exc())
            )
    return failures


def unload(script_id):
    """Tear down per-script state.

    Drops the script's globals, removes its ``sys.path`` entry (for
    projects), purges any modules whose ``__file__`` lives under the
    project folder so a subsequent load picks up fresh source, and clears
    the linecache entry for the main file.

    Returns ``True`` if the script was loaded, ``False`` otherwise.
    """
    state = _states.pop(script_id, None)
    if state is None:
        return False

    _rollback(state, state.project_path is not None)
    return True


def is_loaded(script_id):
    return script_id in _states


def loaded_scripts():
    return list(_states.keys())


def _rollback(state, drop_sys_path_entry):
    if drop_sys_path_entry and state.project_path is not None:
        try:
            sys.path.remove(state.project_path)
        except ValueError:
            pass

    if state.project_path is not None:
        prefix = state.project_path
        for name in list(sys.modules):
            module = sys.modules.get(name)
            if module is None:
                continue
            file = getattr(module, "__file__", None)
            if file and file.startswith(prefix):
                del sys.modules[name]

    linecache.cache.pop(state.main_path, None)
    state.globals.clear()


def _collect_hooks(state):
    seen_start = set()
    seen_stop = set()
    for value in state.globals.values():
        if not callable(value):
            continue
        if getattr(value, "start_function", False) and id(value) not in seen_start:
            state.start_hooks.append(value)
            seen_start.add(id(value))
        if getattr(value, "stop_function", False) and id(value) not in seen_stop:
            state.stop_hooks.append(value)
            seen_stop.add(id(value))


def _invoke_hook(hook, java_script):
    code = getattr(hook, "__code__", None)
    if code is not None and code.co_argcount == 0:
        hook()
    else:
        hook(java_script)
