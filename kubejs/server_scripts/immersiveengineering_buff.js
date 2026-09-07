ServerEvents.recipes(event => {
    event.forEachRecipe({ type: 'immersiveengineering:sawmill' }, recipe => {
        let json = recipe.json
        let result = json.get('result')

        if (result === null || !result.isJsonObject()) return

        let resultObj = result.getAsJsonObject()
        let count = resultObj.get('count')

        if (count === null || count.getAsInt() !== 6) return

        resultObj.addProperty('count', 8)
    })

    event.forEachRecipe({ type: 'immersiveengineering:arc_furnace' }, recipe => {
        let json = recipe.json

        let results = json.get('results')
        if (results === null || !results.isJsonArray()) return

        let resultArray = results.getAsJsonArray()

        for (let i = 0; i < resultArray.size(); i++) {
            let result = resultArray.get(i)
            if (!result.isJsonObject()) continue

            let resultObj = result.getAsJsonObject()
            let count = resultObj.get('count')

            if (count === null || count.getAsInt() !== 13) continue

            resultObj.addProperty('count', 15)
        }
    })

    event.forEachRecipe({ type: 'immersiveengineering:crusher' }, recipe => {
        let json = recipe.json

        let result = json.get('result')
        if (result === null || !result.isJsonObject()) return

        let resultObj = result.getAsJsonObject()
        let count = resultObj.get('count')

        if (count === null || count.getAsInt() !== 12) return

        resultObj.addProperty('count', 14)
    })
})

