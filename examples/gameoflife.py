import pyspigot as ps
from org.bukkit import Bukkit
from org.bukkit import Location
from org.bukkit import Material
from org.bukkit import ChatColor

adjacent_blocks = [(1,1),(1,0),(1,-1),(0,-1),(-1,-1),(-1,0),(-1,1),(0,1)]

# Script config variables, change these to your liking
tick_speed = 4
dead_material = Material.BLACK_WOOL
alive_material = Material.WHITE_WOOL
game_world = Bukkit.getWorld('world')
start_x = -200
start_z = -200
end_x = 200
end_z = 200
y = 100

grid = []
game_loop = None

def reset_grid():
    for x in xrange(start_x, end_x):
        for z in xrange(start_z, end_z):
            block = game_world.getBlockAt(x, y, z)
            block.setType(dead_material)

def init_grid():
    global grid
    grid = []
    for x in xrange(start_x, end_x):
        inner_grid = []
        for z in xrange(start_z, end_z):
            block = game_world.getBlockAt(x, y, z)
            state = 0
            if block.getType() == alive_material:
                state = 1
            inner_grid.append({'x': x, 'z': z, 'state': state, 'next_state': state, 'check_next': 0})
        grid.append(inner_grid)

def compute_next_checks():
    for i in xrange(len(grid)):
        for j in xrange(len(grid[i])):
            cell = grid[i][j]
            if cell['state'] == 1:
                cell['check_next'] = 1
                for adjacent in adjacent_blocks:
                    grid[i + adjacent[0]][j + adjacent[1]]['check_next'] = 1

def compute_transition(i, j):
    try:
        cell = grid[i][j]
        alive_neighbors = 0
        for face in adjacent_blocks:
            relative_cell = grid[i + face[0]][j + face[1]]
            if relative_cell['state'] == 1:
                alive_neighbors += 1
        if cell['state'] == 1:
            if alive_neighbors < 2:
                return 0
            elif alive_neighbors == 2 or alive_neighbors == 3:
                return 1
            elif alive_neighbors > 3:
                return 0
        elif cell['state'] == 0:
            if alive_neighbors == 3:
                return 1
            else:
                return 0
    except IndexError:
        return 0

def compute_next_grid():
    for i in xrange(len(grid)):
        for j in xrange(len(grid[i])):
            cell = grid[i][j]
            if cell['check_next'] == 1:
                cell['next_state'] = compute_transition(i, j)
                cell['check_next'] = 0

def apply_grid():
    for x in xrange(len(grid)):
        for z in xrange(len(grid[x])):
            cell = grid[x][z]
            if cell['state'] != cell['next_state']:
                block = game_world.getBlockAt(cell['x'], y, cell['z'])
                if cell['next_state'] == 1:
                    block.setType(alive_material)
                elif cell['next_state'] == 0:
                    block.setType(dead_material)
                cell['state'] = cell['next_state']

def tick():
    compute_next_checks()
    compute_next_grid()
    apply_grid()

def tick_manual():
    init_grid()
    tick()

def start_game():
    global game_loop
    if (game_loop == None):
        init_grid()

        game_loop = ps.scheduler.scheduleRepeatingTask(tick, 0, tick_speed)

def stop_game():
    global game_loop
    if game_loop != None:
        game_loop = ps.scheduler.stopTask(game_loop)
        game_loop = None

def game_command(sender, label, args):
    if sender.hasPermission("gameoflife.command"):
        if len(args) > 0:
            if args[0] == 'start':
                if game_loop == None:
                    start_game()
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', '&aStarted the game.'))
                else:
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', '&cThe game is already running.'))
            elif args[0] == 'stop':
                if game_loop != None:
                    stop_game()
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', '&aStopped the game.'))
                else:
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', '&cThe game is not running.'))
            elif args[0] == 'reset':
                if game_loop == None:
                    reset_grid()
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', '&aReset the grid.'))
                else:
                    stop_game()
                    reset_grid()
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', '&aStopped the game and reset the grid.'))
            elif args[0] == 'tick':
                if game_loop == None:
                    tick_manual()
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', '&aPerformed a manual tick of the game.'))
                else:
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', '&cUnable to perform a manual tick when the game is running.'))
            elif args[0] == 'setspeed':
                if len(args) > 1:
                    try:
                        global tick_speed
                        tick_speed = int(args[1])
                        if game_loop != None:
                            stop_game()
                            start_game()
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', '&aSet the game speed to ' + args[1] + '.'))
                    except ValueError:
                        sender.sendMessage(ChatColor.GREEN + 'The speed must be a number.')
                else:
                    sender.sendMessage(ChatColor.GREEN + 'Usage: /gameoflife setspeed <number>')
            else:
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', '&cUsage: /gameoflife <start|stop|reset|tick|setspeed>'))
        else:
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', '&cUsage: /gameoflife <start|stop|reset|tick|setspeed>'))
    return True

ps.command.registerCommand(game_command, 'gameoflife', 'Conway\'s Game of Life', '/gameoflife <argument>', ['gol'])