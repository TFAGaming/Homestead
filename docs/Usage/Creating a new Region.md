# Create a new Region

To protect an area, you must create a region. Use `/region create [region name]` to create a new region.

## Claiming chunks

Go inside your area and claim all the required chunks to protect your builds using the command `/claim`. If you are in the default group, you only have 10 chunks to claim (changeable in **config.yml**).

Each claimed chunk will be part of your region, and nobody else can take your region's chunks.

## Unclaiming chunks

To unclaim a chunk, use `/unclaim`.

!!! warning

    If you unclaim a chunk, any sub-area that intersects or is inside that chunk will be deleted.

If you are far away from the claimed chunks and you want to unclaim them quickly, use `/region claimlist` to open the menu.
