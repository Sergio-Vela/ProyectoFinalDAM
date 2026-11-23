// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorNativeTools",
    platforms: [.iOS(.v15)],
    products: [
        .library(
            name: "CapacitorNativeTools",
            targets: ["NativeToolsPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "8.0.0")
    ],
    targets: [
        .target(
            name: "NativeToolsPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/NativeToolsPlugin"),
        .testTarget(
            name: "NativeToolsPluginTests",
            dependencies: ["NativeToolsPlugin"],
            path: "ios/Tests/NativeToolsPluginTests")
    ]
)