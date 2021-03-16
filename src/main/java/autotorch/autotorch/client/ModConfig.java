package autotorch.autotorch.client;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.*;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name="autotorch")
public class ModConfig implements ConfigData {

    @Comment("Enable the Auto Torch Mod")
    boolean enabled = true;

    @Comment("The light level below which the torches are placed.")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 14)
    int lightLevel = 4;

}
