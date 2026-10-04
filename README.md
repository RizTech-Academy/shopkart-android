# ShopKart Android

Android client for the [ShopKart API](https://github.com/RizTech-Academy/shopkart-api) — a worked example of Clean Architecture in Kotlin.

Kotlin, Jetpack Compose, multi-module, with the dependency rule enforced by the build graph rather than by convention.

> **Status: complete and running.** All six modules are built, 35 tests pass, and the
> app has been run end to end against the live API — browse, search, filter, open a
> product, add to basket, change quantities, check out.

---

## Running it

Requires **JDK 17** and the **Android SDK (API 35)**. Android Studio Ladybug or newer.

```bash
git clone https://github.com/RizTech-Academy/shopkart-android.git
cd shopkart-android
./gradlew :core:domain:test        # 21 tests, no emulator needed
```

`local.properties` is gitignored. Android Studio writes it on first open; from the command line, create it yourself:

```bash
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties   # macOS
```

### Connecting to the backend

Start the API first — it runs locally with no configuration:

```bash
git clone https://github.com/RizTech-Academy/shopkart-api.git
cd shopkart-api && npm install && npm run dev     # http://localhost:3000
```

**The emulator cannot reach `localhost`.** Inside the emulator, `localhost` is the emulated
device itself, not your machine. Android exposes the host at a special address:

| Running on | Base URL |
| --- | --- |
| Android emulator | `http://10.0.2.2:3000/` |
| Physical device on the same Wi-Fi | `http://<your-machine-ip>:3000/` |
| Unit tests (MockWebServer) | injected per test |

`10.0.2.2` is the emulator's alias for the host loopback. This is the single most common
stumbling block when wiring an Android app to a local backend, and it fails with a bare
"connection refused" that gives no hint as to why.

For a physical device, find your machine's IP with `ipconfig getifaddr en0` (macOS) or
`hostname -I` (Linux), and make sure both are on the same network.

Cleartext HTTP is permitted for `10.0.2.2` and local addresses in the debug build only —
release builds require HTTPS.

---

## Architecture

```
:core:domain         Pure Kotlin. Models, repository interfaces, use cases.
                     No Android dependency — enforced by the module type.
:core:data           Retrofit + Room. Implements the domain's interfaces.
:core:designsystem   Theme and shared Compose components.
:feature:catalog     Product list and detail.
:feature:cart        Basket and checkout.
:app                 Navigation and dependency wiring.
```

**`:core:domain` is a plain Kotlin module, not an Android library.** That is deliberate and
it is the load-bearing decision in this project. It means the domain physically *cannot*
import a `Context`, a `Cursor`, or a Compose type — the compiler rejects it. A dependency
rule enforced by the build graph does not need a reviewer to police it.

Everything else follows from that:

**Business rules live in use cases, not ViewModels.** `AddToCartUseCase` refuses an
out-of-stock product; `UpdateCartQuantityUseCase` treats zero as a removal. Every caller —
button, swipe, deep link — inherits the same behaviour, and none can bypass it.

**The cart computes its own totals.** `Cart.subtotal` is derived in the domain, so every
screen showing a subtotal necessarily agrees, and the arithmetic is testable without an
emulator.

**Money is never a `Double`.** `Money` is a value class over integer minor units. Ten
10-cent items sum to exactly `$1.00`; as a `Double` they sum to `0.9999999999999999`. There
is a test that pins this.

**Tests use hand-written fakes, not a mocking framework.** `FakeCartRepository` is a real
implementation, so tests assert on observable behaviour rather than on which methods were
called — and they do not break every time the implementation is refactored.

---

## What is built

All six modules, **35 tests passing**.

| Module | Contains |
| --- | --- |
| `:core:domain` | Models, repository interfaces, use cases. Pure Kotlin — 21 tests |
| `:core:data` | Retrofit, Room, mappers, repository implementations, Hilt modules — 14 tests |
| `:core:designsystem` | Material 3 theme, money formatting, product card, quantity stepper, loading/error/empty states |
| `:feature:catalog` | Product list with debounced search, category and sort filters; product detail |
| `:feature:cart` | Basket, quantity editing, checkout |
| `:app` | Navigation, application class, DI entry point |

### Decisions worth defending

**One transaction per logical change.** `addItem` writes the product row and the cart
row, and the cart query joins the two. Without a transaction Room invalidates after the
first write and emits a cart that does not yet contain the item just added — every
observer briefly renders a stale basket. There is a test that fails without the
transaction.

**A search never overwrites the offline cache.** Only an unfiltered fetch replaces the
cached catalogue. Otherwise going offline after searching for "keyboard" leaves one
product as your entire catalogue. There is a test pinning this too.

**Order lines snapshot the product; cart lines join to it.** A basket must show today's
price. A past order must show what was actually paid, even after the product changes or
is withdrawn.

**Failures are an enum, not an exception.** `DataError.Network` and `DataError.NotFound`
are different things to a user, and `DataResult` makes the caller handle both. Wording
lives in the ViewModel, where it can be localised — not in the data layer.

**`ignoreUnknownKeys` on the JSON.** Without it the server cannot add a field without
breaking every installed app. A test feeds the client a field it has never heard of.

**No `fallbackToDestructiveMigration`.** Silently wiping a user's basket on upgrade is a
data-loss bug that only appears in production. A missing migration should fail loudly in
development instead.

**Cleartext HTTP only for loopback.** `10.0.2.2` and `localhost` are permitted so the
emulator can reach a local API; everything else requires HTTPS, including in debug. A
blanket `cleartextTrafficPermitted="true"` is how a development convenience ships.

---

## Testing approach

Unit tests run on the JVM. Integration tests use **Robolectric** rather than
instrumented tests, so they exercise a real Room database and real Retrofit against
MockWebServer while still running in CI without an emulator.

```bash
./gradlew :core:domain:test            # domain rules, 21 tests
./gradlew :core:data:testDebugUnitTest # repositories against real Room + MockWebServer, 14 tests
./gradlew test                         # everything
```

Repository tests use MockWebServer rather than a mocked Retrofit interface, because the
thing most likely to break is the JSON contract and a mocked interface cannot catch that.
Room runs in memory rather than behind a mocked DAO, for the same reason.

### Running the app against a local API

```bash
./gradlew :app:installDebug -Pshopkart.baseUrl=http://10.0.2.2:3000/
```

---

Built by [RizTech Academy](https://www.riztechacademy.com) — offshore software development for startups and small businesses.
