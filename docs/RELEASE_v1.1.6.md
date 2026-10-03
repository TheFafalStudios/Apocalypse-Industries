# Apocalypse Industries v1.1.6

Targeted addon release based on published v1.1.5. Runtime additions:

- Gunsmithing Enchantments 1.1.0: gun enchanting plus 65% armor-point bypass for the two piercing-round types.
- Importer Safety Fix 1.0.0: item-manifest selection and purchase-loop hang prevention.

Exact runtime hashes are in index.toml. Existing dependency references, configuration, quests and recipe files are unchanged. Both client and server must restart to load the addons. The native test reports distinguish tested behavior from full-pack/live-server gameplay.

Server file deployment and rollback evidence: Apocalypse Industries versions/v1.1.6. Deployment proves file installation; startup/activation and broader baseline compatibility must be reported separately. The existing uncertain server baseline is not certified by this targeted release.

Combined native dedicated-server verification: both final addons loaded together, 589 gun/armor assertions plus 56 importer assertions passed, and the disposable server shut down cleanly. Distribution validation checked all 1182 Packwiz entries and the root hash. This is isolated testing, not a full live-server gameplay claim.
