import AppKit

// 1024x1024 无 alpha 位图（App Store 图标要求）
let W = 1024, H = 1024
guard let rep = NSBitmapImageRep(
    bitmapDataPlanes: nil, pixelsWide: W, pixelsHigh: H,
    bitsPerSample: 8, samplesPerPixel: 3, hasAlpha: false, isPlanar: false,
    colorSpaceName: .deviceRGB, bytesPerRow: 0, bitsPerPixel: 0
) else { exit(1) }

rep.size = NSSize(width: W, height: H)
NSGraphicsContext.saveGraphicsState()
NSGraphicsContext.current = NSGraphicsContext(bitmapImageRep: rep)

// ===== 1. 品牌红渐变背景 =====
let gradient = NSGradient(colors: [
    NSColor(red: 1.00, green: 0.44, blue: 0.42, alpha: 1),  // #ff6f6b 顶部浅红
    NSColor(red: 0.89, green: 0.22, blue: 0.24, alpha: 1),  // #e4393c 品牌红
    NSColor(red: 0.72, green: 0.10, blue: 0.12, alpha: 1),  // 底部深红
])!
gradient.draw(in: NSRect(x: 0, y: 0, width: W, height: H), angle: -90)

// ===== 2. 白色云朵（上半部）=====
let cloud = NSBezierPath()
cloud.append(NSBezierPath(roundedRect: NSRect(x: 262, y: 380, width: 500, height: 210),
                          xRadius: 105, yRadius: 105))
cloud.appendOval(in: NSRect(x: 300, y: 470, width: 230, height: 230))
cloud.appendOval(in: NSRect(x: 430, y: 520, width: 300, height: 300))
cloud.appendOval(in: NSRect(x: 560, y: 450, width: 240, height: 240))
NSColor.white.setFill()
cloud.fill()

// ===== 3. 购物袋（白色描边，云朵下方）=====
let bag = NSBezierPath()
bag.move(to: NSPoint(x: 400, y: 640))
bag.curve(to: NSPoint(x: 624, y: 640),
          controlPoint1: NSPoint(x: 400, y: 760),
          controlPoint2: NSPoint(x: 624, y: 760))
bag.append(NSBezierPath(roundedRect: NSRect(x: 342, y: 210, width: 340, height: 430),
                        xRadius: 52, yRadius: 52))
bag.lineWidth = 34
bag.lineCapStyle = .round
bag.lineJoinStyle = .round
NSColor.white.setStroke()
bag.stroke()

// ===== 4. 袋身中央红色圆点（品牌负空间点缀）=====
let dot = NSBezierPath(ovalIn: NSRect(x: 512 - 40, y: 390, width: 80, height: 80))
NSColor(red: 0.89, green: 0.22, blue: 0.24, alpha: 1).setFill()
dot.fill()

NSGraphicsContext.restoreGraphicsState()

guard let png = rep.representation(using: .png, properties: [:]) else { exit(1) }
let out = CommandLine.arguments.count > 1 ? CommandLine.arguments[1] : "/tmp/AppIcon.png"
try! png.write(to: URL(fileURLWithPath: out))
print("saved: \(out)")
