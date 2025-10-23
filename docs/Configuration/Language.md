# Language

Homestead doesn't provide other language files except for the **English (US)** language file.

If you want to change the language, you can use AI tools to translate the English file to the language you want.

To change the language, go to **config.yml** and change this value:

```yaml
language: "en-US" # <-- The language file name
```

Remember when translating your language file, do not change the variable names. For example, do not change
`{region-name}` to `{nom-du-région}` (in French or any other language).
