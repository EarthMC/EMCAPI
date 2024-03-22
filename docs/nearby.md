# Nearby Endpoint
Accessed at https://api.earthmc.net/v3/aurora/nearby/town?town=Teslin&radius=1000 or https://api.earthmc.net/v3/aurora/nearby/coordinate?x=20000&z=-5230&radius=3500

Returns a JSON array of all the towns within the specified radius around the specified town or coordinate, empty if none are present.

This is measured town block to town block. For example, from the town block at the specified coordinate to the home block of the town being checked. This can result in "outposts" being skipped