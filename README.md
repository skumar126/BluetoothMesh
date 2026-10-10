# BluetoothMesh

Android app for discovering nearby Bluetooth Mesh devices and storing mesh network records locally.

It scans for BLE advertisements that carry the Mesh Provisioning (`0x1827`) or Mesh Proxy (`0x1828`) GATT service, lists them with RSSI and provisioned/unprovisioned status, and persists mesh names with creation and modification timestamps in Room.

Provisioning, configuration, and mesh messaging are **not implemented yet**. This project is the scanner and local-network catalog layer for a future Mesh provisioner.
