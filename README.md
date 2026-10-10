# BluetoothMesh

Android app for discovering nearby Bluetooth Mesh devices and storing mesh network records locally.

It scans for BLE advertisements that carry the Mesh Provisioning (`0x1827`) or Mesh Proxy (`0x1828`) GATT service, lists them with RSSI and provisioned/unprovisioned status, and persists mesh names with creation and modification timestamps in Room.

Provisioning, configuration, and mesh messaging are **not implemented yet**. This project is the scanner and local-network catalog layer for a future Mesh provisioner.

## Features

- BLE scan filtered to Mesh Provisioning and Mesh Proxy services
- Runtime Bluetooth permissions (scan/connect on Android 12+, location on older APIs)
- System prompt to turn Bluetooth on when it is off
- Device list with name, MAC address, RSSI, and UNPROVISIONED / PROVISIONED status
- Room database for mesh networks: name, created date, modified date

## Requirements

- Android 7.0 (API 24) or later
- A device with Bluetooth LE (`android.hardware.bluetooth_le`)
- Android Studio compatible with AGP 9.4.1 / Kotlin 2.2.10

## How it works

```
MainActivity
  → request permissions
  → prompt to enable Bluetooth if needed
  → 10-second BLE scan (UUIDs 1827 and 1828)
  → DeviceAdapter list

BluetoothMeshApp
  → MeshDatabase (Room)
  → MeshRepository
  → mesh_networks table
```

### BLE services

| UUID | Role |
| --- | --- |
| `00001827-0000-1000-8000-00805F9B34FB` | Mesh Provisioning (unprovisioned node) |
| `00001828-0000-1000-8000-00805F9B34FB` | Mesh Proxy (typically already on a network) |

Status in the list is inferred from which service UUID is advertised. Advertising Proxy does **not** mean this phone provisioned the node or that it belongs to a specific network.

### Local storage

Table `mesh_networks` in database `mesh_database`:

| Column | Description |
| --- | --- |
| `id` | Auto-generated primary key |
| `name` | Mesh network name |
| `created_at` | Creation time (epoch millis) |
| `modified_at` | Last update time (epoch millis) |

Use the repository from the application class:

```kotlin
val repository = (application as BluetoothMeshApp).meshRepository

lifecycleScope.launch {
    val id = repository.createMesh("Home Mesh")
    repository.renameMesh(id, "Office Mesh")
}

repository.observeMeshes().collect { meshes -> /* ... */ }
```

## Project structure

```
app/src/main/java/com/mesh/myapplication/
  MainActivity.kt                 # Scan UI and Bluetooth enable flow
  MeshProvisioningFragment.kt     # Alternate scan screen (not hosted yet)
  adapter/DeviceAdapter.kt        # Scan result list
  data/
    MeshNetwork.kt                # Room entity
    MeshNetworkDao.kt
    MeshDatabase.kt
    MeshRepository.kt
  BluetoothMeshApp.kt             # Application + repository singleton
```

## Permissions

Declared in `AndroidManifest.xml`:

- `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT` (Android 12+)
- `BLUETOOTH` (API ≤ 30)
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` (pre-Android 12 scan)

The app requests these at runtime before scanning.

## Build

Open the project in Android Studio and run the `app` configuration, or:

```bash
./gradlew :app:assembleDebug
```

## Current limitations

- No Mesh provisioner SDK (for example Nordic Android Mesh) is integrated
- Device click only shows a toast; provisioning is a stub
- `MeshProvisioningFragment` is unused from `MainActivity`
- Mesh records are stored in Room but there is not yet a UI to create or list them
- Scan results are not yet deduplicated by MAC address when RSSI changes

## License

Private / unspecified.
