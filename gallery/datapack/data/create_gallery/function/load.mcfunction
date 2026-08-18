scoreboard objectives add create_gallery dummy
execute unless score #ready create_gallery matches 1 run function create_gallery:build
