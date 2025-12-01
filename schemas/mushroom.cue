package mooshroom

#MushroomData: {
    common_names: [...string]
    genus: string
    species: string
    humidity: {
        min: >0
        max: <=100
    }
    temperature: {
        min: >0
        max: <=100
    }
}