# ItemBasedCommandBlocker

## Téléchargement
Pour télécharger le plugin ou voir un exemple de configuration :
http://dragorn421.fr/ibcb/

## Commandes et permissions
Aucune commande ni aucune permission n'accompagne ce plugin. Des permissions peuvent être envisagées si besoin est.

## Configuration
```yaml
#Si la comparaison d'un filtre est omise cette comparaison est utilisée à la place.
#Les valeurs de comparaison valides sont "equals", "contains" et "startsWith".
default-comparison: equals
#Chaque sous-section de filters représente un filtre. Chaque filtre permet de bloquer une commande d'une certaine manière.
#Chaque sous-section doit posséder un nom différent. Le nom choisi est sans importance sur le fonctionnement du plugin.
filters:
  #Cette exemple bloque la commande /repair (et pas /repair avec un argument quelconque) si le joueur possède une pioche en diamant enchantée Silk Touch
  a:
    #La commande à bloquer si l'item est détecté
    command: /repair
    #Le type de comparaison à utiliser pour détecter la commande
    #equals : la commande citée en configuration et la commande exécutée par le joueur doivent être strictement les mêmes (insensible à la casse)
    #contains : la commande exécutée par le joueur doit comprendre la commande citée en configuration
    #startsWith : la commande exécutée par le joueur doit commencer par la commande citée en configuration
    comparison-type: equals
    #Le type de l'item recherché. Peut être omis, auquel cas tous les types d'items seront considérés par ce filtre.
    #La valeur doit correspondre à une valeur sur cette page https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html
    type: DIAMOND_PICKAXE
    #La data value de l'item recherché. Mettre data à -1 permet d'ignorer la data value.
    data: -1
    #Les liste des enchantements, recherchés ou non sur l'item
    #Les valeurs doivent correspondre aux valeurs sur cette page https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/enchantments/Enchantment.html
    #with-enchantments permet de bloquer la commande pour un item possédant au moins tous les enchantments de la liste
    with-enchantments:
    - SILK_TOUCH
    #without-enchantments permet de ne pas bloquer la commande pour un item possédant au moins un des enchantments de la liste
    without-enchantments: []
```
##### Liens
https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html
https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/enchantments/Enchantment.html
