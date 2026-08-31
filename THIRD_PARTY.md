# Third-party components

Nothing in this table is bundled in the production JAR.

| Component | Exact accepted identity | Declared license | Use |
| --- | --- | --- | --- |
| BlueMap | Feature backport `5.22-feature.backport-5.23-stateless-java-web-server-46`, commit `7e07f4e74ec1e92a6ead9aa1e66054af3e133aac`; API `285c9a60eff3ac2b0cab308ce1058d1565be0971` | MIT | Compile/test API |
| BlueMap Add-on Adapter API | `0.1.0-alpha.2`, commit `e81f08bc4bfbf02d810ec8949a019130e2e61634` | MIT | Four exact source files compiled into this add-on |
| Create | `6.0.10`, SHA-256 `ef87fe5709f1ba1f5b8bb20a2925b5afb4669e178fd6d8bf10c167759eefe37a` | Code MIT; assets All Rights Reserved | Operator-installed runtime resources |
| Create Aquatic Ambitions | `2.0.4`, SHA-256 `d50180fd30dc7f034ea4ad5185d18cfa652457be1d8e7a45f0b491d0e6642d44` | MIT | Optional exact runtime profile |
| Create Crafts & Additions | `1.6.0`, SHA-256 `41876c3780b70365a1848994d146a73423cc19fbe86485885795d9e7d855e7e9` | MIT | Optional exact runtime profile |
| Create Hypertube | `0.6.0`, SHA-256 `7bdb8979c7ff7d3b29f7a23771b6ae4870a6dcb7ce2e4a3214fdd6059aacace8` | Apache-2.0 | Optional exact runtime profile |
| Create: Enchantment Industry | `2.5.0`, SHA-256 `02a184531c11433cd6521f612982568398aaf510b8ff51e052a78cf7d09d9a49` | LGPL-3.0-or-later | Optional exact runtime profile |
| JetBrains annotations | `23.0.0` | Apache-2.0 | Compile only |
| JUnit Jupiter | `5.11.4` | EPL-2.0 | Test only |
| Gradle | `9.6.1` | Apache-2.0 | Build only |

Create: Enchantment Industry requires Create: Dragons Plus at runtime. The
accepted staging dependency was `1.11.4`, SHA-256
`80687f22daa95fa6240631097688f1e0295a5d31473d9aa56a14d360d863e98b`;
the add-on does not directly route Dragons Plus resources.

The production JAR contains only first-party classes, add-on metadata, and the
project MIT license. Exact artifact sizes, distribution identities, source
correlations, and audited no-route extensions are recorded in
`provenance/upstreams.json`.
