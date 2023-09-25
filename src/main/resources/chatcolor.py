#     Copyright 2023 magicmq
#
#     Licensed under the Apache License, Version 2.0 (the "License");
#     you may not use this file except in compliance with the License.
#     You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
#     Unless required by applicable law or agreed to in writing, software
#     distributed under the License is distributed on an "AS IS" BASIS,
#     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#     See the License for the specific language governing permissions and
#     limitations under the License.

# Wrapper functions for org.bukkit.ChatColor
from org.bukkit import ChatColor

# Color Codes
def aqua():
    return ChatColor.AQUA

def black():
    return ChatColor.BLACK

def blue():
    return ChatColor.BLUE

def dark_aqua():
    return ChatColor.DARK_AQUA

def dark_blue():
    return ChatColor.DARK_BLUE

def dark_gray():
    return ChatColor.DARK_GRAY

def dark_green():
    return ChatColor.DARK_GREEN

def dark_purple():
    return ChatColor.DARK_PURPLE

def dark_red():
    return ChatColor.DARK_RED

def gold():
    return ChatColor.GOLD

def gray():
    return ChatColor.GRAY

def green():
    return ChatColor.GREEN

def light_purple():
    return ChatColor.LIGHT_PURPLE

def red():
    return ChatColor.RED

def white():
    return ChatColor.WHITE

def yellow():
    return ChatColor.YELLOW

# Formatting codes

def bold():
    return ChatColor.BOLD

def italic():
    return ChatColor.ITALIC

def magic():
    return ChatColor.MAGIC

def reset():
    return ChatColor.RESET

def strikethrough():
    return ChatColor.STRIKETHROUGH

def underline():
    return ChatColor.UNDERLINE

# Wrappers for functions in ChatColor

def get_by_char(code):
    return ChatColor.getByChar(code)

def get_last_colors(input):
    return ChatColor.getLastColors(input)

def strip_color(input):
    return ChatColor.stripColor(input)

def translate_color_codes(alt_color_char, text_to_translate):
    return ChatColor.translateAlternateColorCodes(alt_color_char, text_to_translate)

def value_of(name):
    return ChatColor.valueOf(name)