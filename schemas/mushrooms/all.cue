package mushrooms

import (
    mooshroom "github.com/sethcurry/mooshroom/schemas:mooshroom"
)

#all_mushrooms: [
    {
        common_names: ["Button Mushroom"]
        genus: "Agaricus"
        species: "bisporus"
        humidity: {
            min: 1
            max: 100
        }
        temperature: {
            min: 12.8
            max: 21.1
        }
    }
]

[
    for x in #all_mushrooms
    if true {
        x & mooshroom.#MushroomData
    }
]