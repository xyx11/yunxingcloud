import AppKit
import CoreGraphics

let W = 1024, H = 1024

// ===== 创建 1024x1024 RGBA 位图上下文 =====
let colorSpace = CGColorSpaceCreateDeviceRGB()
guard let ctx = CGContext(
    data: nil, width: W, height: H,
    bitsPerComponent: 8, bytesPerRow: 0,
    space: colorSpace,
    bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue
) else { exit(1) }

// CG 坐标原点在左下角，y 向上

// ===== 1. 品牌红渐变背景（上浅 → 下深）=====
let colors = [
    CGColor(red: 1.00, green: 0.44, blue: 0.42, alpha: 1),
    CGColor(red: 0.89, green: 0.22, blue: 0.24, alpha: 1),
    CGColor(red: 0.72, green: 0.10, blue: 0.12, alpha: 1),
] as CFArray
let gradient = CGGradient(colorsSpace: colorSpace, colors: colors, locations: [0, 0.55, 1])!
ctx.drawLinearGradient(gradient,
                       start: CGPoint(x: 512, y: 1024),
                       end: CGPoint(x: 512, y: 0),
                       options: [])

// ===== 2. 白色云朵（屏幕上方，CG 坐标 y 大）=====
// 云底圆角矩形 + 三个圆
let cloudRect = CGRect(x: 262, y: 590, width: 500, height: 210)
let cloudPath = CGMutablePath()
cloudPath.addRoundedRect(in: cloudRect, cornerWidth: 105, cornerHeight: 105)
cloudPath.addEllipse(in: CGRect(x: 300, y: 500, width: 230, height: 230))
cloudPath.addEllipse(in: CGRect(x: 430, y: 550, width: 300, height: 300))
cloudPath.addEllipse(in: CGRect(x: 560, y: 480, width: 240, height: 240))
ctx.setFillColor(CGColor(red: 1, green: 1, blue: 1, alpha: 1))
ctx.addPath(cloudPath)
ctx.fillPath()

// ===== 3. 购物袋（白色描边，云朵下方）=====
let bagPath = CGMutablePath()
// 提手弧线
bagPath.move(to: CGPoint(x: 400, y: 610))
bagPath.addCurve(to: CGPoint(x: 624, y: 610),
                 control1: CGPoint(x: 400, y: 730),
                 control2: CGPoint(x: 624, y: 730))
// 袋身圆角矩形
bagPath.addRoundedRect(in: CGRect(x: 342, y: 180, width: 340, height: 430),
                       cornerWidth: 52, cornerHeight: 52)
ctx.setStrokeColor(CGColor(red: 1, green: 1, blue: 1, alpha: 1))
ctx.setLineWidth(34)
ctx.setLineCap(.round)
ctx.setLineJoin(.round)
ctx.addPath(bagPath)
ctx.strokePath()

// ===== 4. 袋身中央红色圆点 =====
ctx.setFillColor(CGColor(red: 0.89, green: 0.22, blue: 0.24, alpha: 1))
ctx.fillEllipse(in: CGRect(x: 512 - 40, y: 355, width: 80, height: 80))

// ===== 输出 PNG =====
guard let cgImage = ctx.makeImage() else { exit(1) }
let rep = NSBitmapImageRep(cgImage: cgImage)
guard let png = rep.representation(using: .png, properties: [:]) else { exit(1) }
let out = CommandLine.arguments.count > 1 ? CommandLine.arguments[1] : "/tmp/AppIcon.png"
try! png.write(to: URL(fileURLWithPath: out))
print("saved: \(out)")
