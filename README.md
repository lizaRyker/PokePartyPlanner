# PokePartyPlanner
Program that will build a balanced Pokemon party with a starter Pokemon and user-chosen important stats. Also has a generation limiter. Does not include the latest generation.

Current known bug: if the user limits the generation to any generation before Fairy was a type, the program will likely fail due to the way it chooses which types to include in the final balanced party. The fix involves finding a database file of pre-Fairy generation Pokemon and using that data when building a pre-Fairy party. Ultimately, it would be best if there was different data used for each generation, because each new generation altered the stats of the previous generation(s). That probably won't happen, but the general pre-Fairy fix is planned.
