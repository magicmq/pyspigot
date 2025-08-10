/*
 *    Copyright 2025 magicmq
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dev.magicmq.pyspigot.velocity;


import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import dev.magicmq.pyspigot.PluginListener;
import dev.magicmq.pyspigot.util.player.PlayerAdapter;
import dev.magicmq.pyspigot.velocity.util.player.VelocityPlayer;

import java.util.concurrent.TimeUnit;

public class VelocityListener extends PluginListener {

    @Subscribe
    public void onJoin(PlayerChooseInitialServerEvent event) {
        PlayerAdapter velocityPlayer = new VelocityPlayer(event.getPlayer());
        PyVelocity.get().getProxy().getScheduler().buildTask(PyVelocity.get(), () -> this.onJoin(velocityPlayer)).delay(1L, TimeUnit.SECONDS).schedule();
    }
}
