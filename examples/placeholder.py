from dev.magicmq.pyspigot import PySpigot as ps

#The placeholder in this example scrpipt will be %script:placeholder_<placeholder>%. For example, %script:placeholder_player_banned% to get if the player is banned
def replacer(offline_player, placeholder):
    if offline_player is not None:
        if placeholder == 'player_banned':
            return 'Banned' if offline_player.isBanned() else 'Not banned'
        elif placeholder == 'is_online':
            return 'Online' if offline_player.isOnline() else 'Not online'
        elif placeholder == 'has_player_before':
            return 'Played before' if offline_player.hasPlayedBefore() else 'Not played before'
        elif placeholder == 'uuid':
            return offline_player.getUniqueId().toString()

placeholder = ps.placeholder.registerPlaceholder(replacer)