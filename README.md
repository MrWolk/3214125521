# ALLINShop 1.0.2

Paper 26.2 / Java 25 / Vault.

## Adventure mode
Players in Adventure mode can use registered shop signs with right-click. The plugin handles the click itself and cancels vanilla sign editing. Registered shop signs are protected by event handlers from editing, breaking, pistons and explosions. Players in Adventure mode can still activate them with right click.

## Create
Hold the exact item in the main hand, look at a sign within 6 blocks.

- `/ashop create sell 64 100` — player sells 64 items to server for $100.
- `/ashop create buy 64 150` — player buys 64 items from server for $150.
- `/ashop create both 64 100 150` — both directions. Normal right click = BUY, Shift+right click = SELL max.

Single direction: right click = one transaction; Shift+right click = maximum possible.

## Admin
`/ashop info`, `/ashop remove`, `/ashop setbuy <price>`, `/ashop setsell <price>`, `/ashop setamount <amount>`, `/ashop setitem`, `/ashop reload`

Permissions: `allinshop.use` (true), `allinshop.admin` (op).

SQLite storage: `plugins/ALLINShop/shops.db`.


## Build fix in 1.0.2
GitHub Actions now uses Gradle 9.1.0, which is compatible with running on Java 25.
