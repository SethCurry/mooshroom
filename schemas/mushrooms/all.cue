package mushrooms

import (
	mooshroom "github.com/sethcurry/mooshroom/schemas:mooshroom"
)

#all_mushrooms: [
	{
		common_names: ["Button"]
		genus:   "Agaricus"
		species: "bisporus"
	},
	{
		common_names: ["Beech"]
		genus:   "Hypsizygus"
		species: "tessulatus"
	},
	{
		common_names: ["Horn of Plenty", "Black Chanterelle", "Black Trumpet", "Trumpet of the Dead"]
		genus:   "Craterellus"
		species: "cornucopioides"
	},
	{
		common_names: ["Enoki", "enoki-take", "enoki-dake"]
		genus:   "Flammulina"
		species: "filiformis"
	},
	{
		common_names: ["Sweet Tooth", "Pig's Trotter", "Wood Hedgehog", "Hedgehog Mushroom"]
		genus:   "Hydnum"
		species: "repandum"
	},
	{
		common_names: ["King Trumpet Mushroom", "French Horn Mushroom", "Eryngi", "King Oyster Mushroom", "King Brown Mushroom", "Boletus of the Steppes", "Trumpet Royale", "Ali'i Oyster"]
		genus:   "Pleurotus"
		species: "eryngii"
	},
	{
		common_names: ["Lion's Mane", "Yamabushitake", "Bearded Tooth Fungus", "Bearded Hedgehog"]
		genus:   "Hericium"
		species: "erinaceus"
	},
	{
		common_names: ["hen-of-the-woods", "sheep's head", "ram's head", "maitake"]
		genus:   "Grifola"
		species: "frondosa"
	},
]

[
	for x in #all_mushrooms
	if true {
		x & mooshroom.#MushroomData
	},
]
