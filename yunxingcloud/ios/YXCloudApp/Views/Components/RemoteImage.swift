import SwiftUI

/// 远程图片组件：AsyncImage + 占位/失败态
struct RemoteImage: View {
    let url: URL?
    var cornerRadius: CGFloat = 0

    var body: some View {
        AsyncImage(url: url) { phase in
            switch phase {
            case .success(let image):
                image.resizable().scaledToFill()
            case .failure:
                placeholder
            case .empty:
                ZStack {
                    Color(.systemGray6)
                    ProgressView()
                }
            @unknown default:
                placeholder
            }
        }
        .clipShape(RoundedRectangle(cornerRadius: cornerRadius))
    }

    private var placeholder: some View {
        ZStack {
            Color(.systemGray6)
            Image(systemName: "photo")
                .font(.title2)
                .foregroundStyle(.tertiary)
        }
    }
}
