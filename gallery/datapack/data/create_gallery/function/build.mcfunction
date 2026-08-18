function create_gallery:clear
fill 163 99 163 233 99 211 minecraft:smooth_stone

# Persisted-material copycat steps: four orientations/material states.
setblock 166 100 166 create:copycat_step[facing=south,half=bottom,waterlogged=false]
data merge block 166 100 166 {Material:{Name:"minecraft:bricks"},Item:{id:"minecraft:bricks",count:1}}
setblock 170 100 166 create:copycat_step[facing=east,half=top,waterlogged=false]
data merge block 170 100 166 {Material:{Name:"minecraft:oak_log",Properties:{axis:"x"}},Item:{id:"minecraft:oak_log",count:1}}
setblock 174 100 166 create:copycat_step[facing=north,half=bottom,waterlogged=false]
data merge block 174 100 166 {Material:{Name:"minecraft:cut_copper"},Item:{id:"minecraft:cut_copper",count:1}}
setblock 178 100 166 create:copycat_step[facing=west,half=top,waterlogged=false]
data merge block 178 100 166 {Material:{Name:"minecraft:deepslate_tiles"},Item:{id:"minecraft:deepslate_tiles",count:1}}

# Persisted-material copycat panels on horizontal and vertical faces.
setblock 166 100 172 create:copycat_panel[facing=up,waterlogged=false]
data merge block 166 100 172 {Material:{Name:"minecraft:bricks"},Item:{id:"minecraft:bricks",count:1}}
setblock 170 100 172 create:copycat_panel[facing=down,waterlogged=false]
data merge block 170 100 172 {Material:{Name:"minecraft:oak_planks"},Item:{id:"minecraft:oak_planks",count:1}}
setblock 174 100 172 create:copycat_panel[facing=east,waterlogged=false]
data merge block 174 100 172 {Material:{Name:"minecraft:polished_blackstone"},Item:{id:"minecraft:polished_blackstone",count:1}}
setblock 178 100 172 create:copycat_panel[facing=north,waterlogged=false]
data merge block 178 100 172 {Material:{Name:"minecraft:iron_trapdoor",Properties:{facing:"north",half:"bottom",open:"false",powered:"false",waterlogged:"false"}},Item:{id:"minecraft:iron_trapdoor",count:1}}

# Frozen belts: horizontal, upward and brass-cased runs.
setblock 166 100 180 create:belt[casing=false,facing=east,part=start,slope=horizontal,waterlogged=false]
setblock 167 100 180 create:belt[casing=false,facing=east,part=middle,slope=horizontal,waterlogged=false]
setblock 168 100 180 create:belt[casing=false,facing=east,part=end,slope=horizontal,waterlogged=false]
setblock 173 100 180 create:belt[casing=false,facing=east,part=start,slope=upward,waterlogged=false]
setblock 174 101 180 create:belt[casing=false,facing=east,part=middle,slope=upward,waterlogged=false]
setblock 175 102 180 create:belt[casing=false,facing=east,part=end,slope=upward,waterlogged=false]
setblock 185 100 180 create:belt[casing=true,facing=south,part=start,slope=horizontal,waterlogged=false]
setblock 185 100 181 create:belt[casing=true,facing=south,part=pulley,slope=horizontal,waterlogged=false]
setblock 185 100 182 create:belt[casing=true,facing=south,part=end,slope=horizontal,waterlogged=false]
data merge block 185 100 180 {Controller:{X:185,Y:100,Z:180},IsController:1b,Length:3,Index:0,Casing:"BRASS",Covered:0b}
data merge block 185 100 181 {Controller:{X:185,Y:100,Z:180},IsController:0b,Length:3,Index:1,Casing:"BRASS",Covered:0b}
data merge block 185 100 182 {Controller:{X:185,Y:100,Z:180},IsController:0b,Length:3,Index:2,Casing:"BRASS",Covered:0b}

# Pipe multipart connections, open rims, tank drain and one persisted bracket.
setblock 166 100 189 create:fluid_pipe
setblock 167 100 189 create:fluid_pipe
setblock 168 100 189 create:fluid_pipe
setblock 167 100 188 create:fluid_pipe
setblock 167 100 190 create:fluid_pipe
data merge block 167 100 189 {Bracket:{Name:"create:metal_bracket",Properties:{axis_along_first:"false",facing:"down",type:"pipe"}}}
setblock 174 100 189 create:glass_fluid_pipe[alt=false,axis=x]
setblock 175 100 189 create:glass_fluid_pipe[alt=false,axis=x]
setblock 181 100 189 create:fluid_pipe
setblock 182 100 189 create:fluid_tank
setblock 188 100 189 create:encased_fluid_pipe
setblock 189 100 189 create:fluid_pipe
setblock 195 100 189 create:mechanical_pump[facing=east]
setblock 194 100 189 create:fluid_pipe
setblock 196 100 189 create:fluid_pipe

# Exact omnidirectional CT casing cells; each wall includes edges and center cells.
fill 166 100 198 168 102 198 create:andesite_casing
fill 173 100 198 175 102 198 create:brass_casing
fill 180 100 198 182 102 198 create:copper_casing
fill 187 100 198 189 102 198 create:shadow_steel_casing
fill 194 100 198 196 102 198 create:refined_radiance_casing

# Frozen representative machine parts; activity/readouts/contents intentionally ignored.
setblock 204 100 166 create:mechanical_press[facing=north]
setblock 209 100 166 create:mechanical_mixer
setblock 214 100 166 create:encased_fan[facing=north]
setblock 219 100 166 create:mechanical_drill[facing=north,waterlogged=false]
setblock 224 100 166 create:mechanical_pump[facing=north]

# Native static controls plus connected/multiblock review cells for follow-up judgment.
setblock 204 100 174 create:shaft[axis=x]
data merge block 204 100 174 {Bracket:{Name:"create:wooden_bracket",Properties:{axis_along_first:"false",facing:"down",type:"shaft"}}}
setblock 208 100 174 create:cogwheel[axis=y]
setblock 212 100 174 create:large_cogwheel[axis=y]
data merge block 212 100 174 {Bracket:{Name:"create:metal_bracket",Properties:{axis_along_first:"true",facing:"down",type:"cog"}}}
fill 204 100 182 205 101 183 create:fluid_tank
fill 211 100 182 212 101 183 create:creative_fluid_tank
fill 218 100 182 220 101 183 create:item_vault
fill 226 100 182 227 101 182 create:mechanical_crafter

# Whole/frozen OBJ silhouettes otherwise absent from BlueMap's JSON loader.
setblock 204 100 205 create:flywheel[axis=y]
setblock 210 100 205 create:crushing_wheel[axis=y]
setblock 217 100 205 create:water_wheel[facing=up]
setblock 224 100 205 create:large_water_wheel[axis=y,extension=false]
setblock 231 100 205 create:blaze_burner

scoreboard players set #ready create_gallery 1
tellraw @a [{"text":"Create BlueMap gallery built inside x160..239, z160..215.","color":"aqua"}]
