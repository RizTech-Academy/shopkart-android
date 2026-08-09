# ShopKart Android

Android client for the [ShopKart API](https://github.com/RizTech-Academy/shopkart-api) — a worked example of Clean Architecture in Kotlin.

Kotlin, Jetpack Compose, multi-module, with the dependency rule enforced by the build graph rather than by convention.

> **Status: in progress.** The domain layer is complete and tested. The data, design-system, feature and app modules are being built. What is here is finished work, not scaffolding — see [Progress](#progress).

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

## Progress

**Done — `:core:domain`, 21 tests passing**

| Area | Covered |
| --- | --- |
| `Money` | exact integer arithmetic, negative and non-integer rejection, ordering |
| `Cart` | subtotal across quantities, item counts, invalid line rejection |
| Cart use cases | add, increment on re-add, out-of-stock refusal, zero-removes, observation |
| `PlaceOrderUseCase` | totals, cart emptied on success, empty-cart rejection |

**In progress — `:core:data`**
Retrofit service, DTOs, Room entities and DAOs are written. Repository implementations,
mappers and DI wiring remain.

**Not started**
`:core:designsystem`, `:feature:catalog`, `:feature:cart`, `:app`, and the Robolectric
integration tests (real Room, real Retrofit against MockWebServer).

---

## Testing approach

Unit tests run on the JVM. Integration tests will use **Robolectric** rather than
instrumented tests, so they exercise a real Room database and real Retrofit against
MockWebServer while still running in CI without an emulator.

```bash
./gradlew :core:domain:test    # domain rules
./gradlew test                 # everything, once the modules land
```

---

Built by [RizTech Academy](https://www.riztechacademy.com) — offshore software development for startups and small businesses.
