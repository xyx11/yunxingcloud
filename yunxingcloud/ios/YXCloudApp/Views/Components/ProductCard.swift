import SwiftUI

/// 商品卡片：图 + 名称（2行）+ 价格 + 销量
struct ProductCard: View {
    let product: Product
    var onTap: () -> Void = {}

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            RemoteImage(url: ImageURL.resolve(product.mainImage), cornerRadius: 8)
                .aspectRatio(1, contentMode: .fill)
                .clipped()

            Text(product.name)
                .font(.system(size: 13))
                .lineLimit(2)
                .multilineTextAlignment(.leading)
                .frame(height: 36, alignment: .top)

            HStack(alignment: .firstTextBaseline, spacing: 4) {
                PriceText(fen: product.price, size: 15)
                if let sales = product.sales, sales > 0 {
                    Text("已售\(sales > 10000 ? "\(sales / 10000)万+" : "\(sales)")")
                        .font(.system(size: 10))
                        .foregroundStyle(.secondary)
                }
            }
        }
        .contentShape(Rectangle())
        .onTapGesture(perform: onTap)
    }
}
