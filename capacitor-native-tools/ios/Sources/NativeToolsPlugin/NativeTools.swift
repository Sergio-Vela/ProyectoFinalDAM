import Foundation

@objc public class NativeTools: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
