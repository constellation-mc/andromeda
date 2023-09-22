package me.melontini.andromeda.util.data;

import java.util.List;

public class ItemBehaviorData {
    public CommandHolder on_entity_hit;
    public CommandHolder on_block_hit;
    public CommandHolder on_miss;
    public CommandHolder on_any_hit;
    public boolean spawn_item_particles;
    public boolean spawn_colored_particles;
    public ParticleColors particle_colors;

    public record ParticleColors(int red, int green, int blue) {
    }

    public record CommandHolder(List<String> item_commands, List<String> user_commands, List<String> server_commands,
                                List<String> hit_entity_commands, List<String> hit_block_commands) {
    }
}
