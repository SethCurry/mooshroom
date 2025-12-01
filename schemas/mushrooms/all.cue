package mushrooms

import (
    mooshroom "github.com/sethcurry/mooshroom/schemas:mooshroom"
)

#all_mushrooms: [
    {
        common_names: ["Button Mushroom"]
        genus: "Agaricus"
        species: "bisporus"
    }
    {
        common_names: ["Beech Mushroom"]
        genus: "Hypsizygus"
        species: "tessulatus"
    }
    {
        common_names: ["Horn of Plenty", "Black Chanterelle", "Black Trumpet", "Trumpet of the Dead"]
        genus: "Craterellus"
        species: "cornucopioides"
    }
]

[
    for x in #all_mushrooms
    if true {
        x & mooshroom.#MushroomData
    }
]