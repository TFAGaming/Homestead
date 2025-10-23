# Menus

Menu, or GUI (**G**raphical **U**ser **I**nterface), is a user interface that allows users to interact without the need to run commands. In Minecraft, GUIs are based on chest inventories.

## Editing Menu Titles

To edit a menu's title, go to `menu-titles` in the **menus.yml** file:

```yaml
menu-titles:
  0: "Regions"
  1: "Region: &2{region}" # Minecraft color codes are supported
  2: "Player Flags (Global)"
  ...
```

## Editing Menu Buttons

Below `menu-titles`, you will find `buttons`, which are the buttons for all the menus.

```yaml
buttons:
  0:
    name: "&cBack"
    lore:
      - "&7Return to the previous menu."
    type: RED_STAINED_GLASS_PANE
  1:
    name: "&9Previous"
    lore:
      - "&7Go to the previous page."
    type: ARROW
  ...
```

### Button Types

For a button type, you can use Minecraft's materials like Diamonds (`DIAMOND`), Iron Sword (`IRON_SWORD`)... etc. If the material is not found, it will use Barrier as the default type.

!!! warning "Undefined Material Names"

    If you are using a Minecraft version that is higher than the API version 1.21 and using a material name that doesn't exist in version 1.21, it will be automatically replaced with a barrier block.

    Example: Minecraft added the Copper Golem Statue. You cannot use `COPPER_GOLEM_STATUE` because it will return a barrier block.

You can also use player heads! To set a custom texture for a player head, you must use the second parameter for `PLAYEARHEAD-(Texture)`. Here's how to get a player head's texture:

1. Go to [Minecraft Heads](https://minecraft-heads.com/).
2. Search for a head, such as "Grass", and select the one you like the most. In this example, we will choose this head: [Click here!](https://minecraft-heads.com/custom-heads/head/81357-grass-block)
3. Scroll down until you find Minecraft URL, and then copy the URL.
4. The URL format is like this: `http://textures.minecraft.net/texture/(Texture)`. Remove all characters before (Texture) and voila! You have got the texture ID (something like this: `f9e986ccac3dc804f1bebe054dfb3e800480b7e08b2e7c6a86c84621c756c142`), so copy it.
5. Now, use this format in the button's type: PLAYERHEAD-(Texture), and replace (Texture) with the one you copied, so it should look like this: `PLAYERHEAD-f9e986ccac3dc804f1bebe054dfb3e800480b7e08b2e7c6a86c84621c756c142`.

