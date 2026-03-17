# 🐛 Fixed Bugs

The following bugs were identified and fixed in the Product API service:

### 1. Multiplicative Discount Calculation
- **Bug**: The system was summing up discount percentages (`sumOf { it.percent }`) before applying them to the base price.
- **Fix**: Applied discounts multiplicatively using `fold`.
- **Formula**: `basePrice × (1 - d1%) × (1 - d2%) × ... × (1 + VAT%)`.
- **File**: `ProductService.kt`

### 2. Missing JSON Serialization Configuration
- **Bug**: The Ktor server and client were failing to serialize/deserialize JSON because the `ContentNegotiation` plugin was missing, and the Kotlin serialization plugin was not applied in Gradle.
- **Fix**:
    - Added `kotlin("plugin.serialization")` to `build.gradle.kts`.
    - Installed `ContentNegotiation` with `json()` in `Application.kt`.
- **Files**: `build.gradle.kts`, `Application.kt`

### 3. Concurrency Race Condition
- **Bug**: The `applyDiscount` method used a "read then write" pattern which is not atomic. Concurrent requests could read the same initial state and overwrite each other's updates.
- **Fix**: Replaced the logic with a single atomic MongoDB `findOneAndUpdate` operation using `$push` with a `$not $elemMatch` filter to ensure both atomicity and idempotency.
- **File**: `ProductRepository.kt`

### 4. Dependency Version Consistency
- **Bug**: Hardcoded Testcontainers version in `build.gradle.kts` was inconsistent with the version catalog.
- **Fix**: Updated `build.gradle.kts` to use version catalog references.

---
### ✅ Final Verification Status: **PASSED**
- All 12 test cases (unit and integration) are passing.
- Verified multiplicative discount logic, JSON serialization, and repository atomicity.
