import SwiftUI

/// 价格文本：后端价格单位是分，这里统一转元显示（千分位 + 2 位小数）
struct PriceText: View {
    let fen: Int
    var size: CGFloat = 15
    var color: Color = AppConfig.brandRed
    var showYuanSymbol: Bool = true

    var body: some View {
        Text(Self.format(fen: fen, showYuanSymbol: showYuanSymbol))
            .font(.system(size: size, weight: .bold))
            .foregroundStyle(color)
    }

    static func format(fen: Int, showYuanSymbol: Bool = true) -> String {
        let yuan = Decimal(fen) / 100
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        let num = formatter.string(from: yuan as NSDecimalNumber) ?? "\(yuan)"
        return showYuanSymbol ? "¥\(num)" : num
    }
}
