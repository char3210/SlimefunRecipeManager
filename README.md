# SlimefunRecipeManager

this [slimefun](https://github.com/Slimefun/Slimefun4) addon lets you change slimefun recipes 

## Features

- change recipe and output of base slimefun items
- change recipe of items in addons (untested)

## Planned Features

- allow changing recipe types
- change recipes during runtime without /reload
  - in-game gui
- further customization for researches

## Bugs

- a lot probably (please report via github issues)

## Usage

When first installing this addon, do `/sfrm genrecipes` to generate the default recipes in `recipes.yml`. Once you open it, you should see something like this:

```yaml
CARGO_MOTOR:
  amount: 4
  recipe:
    - HARDENED_GLASS
    - ELECTRO_MAGNET
    - HARDENED_GLASS
    - SILVER_INGOT
    - ELECTRIC_MOTOR
    - SILVER_INGOT
    - HARDENED_GLASS
    - ELECTRO_MAGNET
    - HARDENED_GLASS
...
```

The `amount` specifies the output amount of the recipe (e.g. right now it outputs 4 cargo motors)<br>
Note: if no amount is specified, it will default to its previous amount. <br>

The recipe is a list of 9 material id's. They can be of either Slimefun items, addon items, or vanilla items.<br>
If you want to specify no ingredient, simply use `AIR`. <br>
You can find a list of Slimefun item ids [here](https://sf-items.walshy.dev/). <br>
You can find a list of vanilla materials [here](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html). <br>
