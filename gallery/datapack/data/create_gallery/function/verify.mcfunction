execute if data block 166 100 166 Material{Name:"minecraft:bricks"} if data block 166 100 166 Item{id:"minecraft:bricks"} run tellraw @s {"text":"copycat step Material+Item pair present","color":"green"}
execute if data block 178 100 172 Material{Name:"minecraft:iron_trapdoor"} if data block 178 100 172 Item{id:"minecraft:iron_trapdoor"} run tellraw @s {"text":"copycat panel Material+Item pair present","color":"green"}
execute if block 166 100 180 create:belt run tellraw @s {"text":"belt samples present","color":"green"}
execute if block 185 100 180 create:belt[casing=true,facing=south,part=start,slope=horizontal] if block 185 100 181 create:belt[casing=true,facing=south,part=pulley,slope=horizontal] if block 185 100 182 create:belt[casing=true,facing=south,part=end,slope=horizontal] if data block 185 100 180 {Casing:"BRASS"} if data block 185 100 181 {Casing:"BRASS"} if data block 185 100 182 {Casing:"BRASS"} run tellraw @s {"text":"brass-cased belt pulley network present","color":"green"}
execute if data block 167 100 189 Bracket run tellraw @s {"text":"pipe bracket sample present","color":"green"}
execute if block 167 100 189 create:fluid_pipe run tellraw @s {"text":"pipe connection matrix present","color":"green"}
execute if block 167 101 198 create:andesite_casing run tellraw @s {"text":"connected casing cells present","color":"green"}
execute if block 204 100 166 create:mechanical_press run tellraw @s {"text":"frozen machine row present","color":"green"}
execute if block 224 100 205 create:large_water_wheel run tellraw @s {"text":"frozen OBJ row present","color":"green"}
