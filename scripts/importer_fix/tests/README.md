# Importer ticket-switch regression

The regression harness runs the real item importer tick and purchase methods in a
disposable NeoForge dedicated server. It tests both electric and mechanical importers.
Only energy/speed and block-update notifications are stubbed; inventories, item
components, prices, currency, ticket wear, selection, and purchase methods are native.

Do not install this test mod in a normal instance: it resets prices in the disposable
server, runs the checks on server startup, and stops the server.

## Cases

- A fresh importer buys diorite.
- Andesite -> diorite -> andesite ticket switching completes, using the new price.
- A selection still present in a multi-item manifest is preserved.
- Removing the ticket clears selection; empty/AIR manifests reset progress safely.
- Direct purchases reject empty stacks, null descriptions, and empty tickets.
- Both tick and direct-purchase paths reject zero, negative, NaN, and infinite prices.
- Zero process count, insufficient funds, and full output consume no ticket uses.
- A 130-item purchase spans output stacks, charges for exactly 130 items, and consumes
  one ticket use.

The harness makes 56 assertions across the two variants. Its first ticket-switch
case hangs with unmodified 1.2.2-1.6.2, so run it only under an external timeout.

## Running

Use Java 21 and an installed NeoForge 21.1.248 dedicated-server runtime.
The script requires an explicit new output directory and accepts only the three
mod JARs needed by the test. It does not copy any existing world or configuration.

    python tests/importer/run.py --java-home PATH_TO_JDK21 --libraries PATH_TO_SERVER_LIBRARIES --minecraft-jar PATH_TO_NEOFORGE_CLIENT_JAR --mod-jar PATH_TO_JACKS_ECONOMY_JAR --create-jar PATH_TO_CREATE_JAR --curios-jar PATH_TO_CURIOS_JAR --workdir NEW_EMPTY_DIRECTORY --accept-eula

For the unmodified JAR, add --expect-hang. Success means the first ticket switch
hangs and the disposable process is terminated after eight seconds. The process
always has a three-minute outer timeout.

Validated with Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10, Curios 9.5.1,
and Jack's Economy 1.2.2-1.6.2. The changed importer classes were compiled against
that installed runtime and substituted into a test-only copy of the mod JAR.
This is a dedicated-server runtime regression test, not a full Gradle build or
interactive client test.
