import SwiftUI

enum DreamSpacing {
    static let xs: CGFloat = 4
    static let sm: CGFloat = 8
    static let md: CGFloat = 16
    static let lg: CGFloat = 24
    static let xl: CGFloat = 32
}


struct DreamColors {
    let primary: Color
    let onPrimary: Color
    let secondary: Color
    let onSecondary: Color
    let background: Color
    let surface: Color
    let surfaceVariant: Color
    let onSurface: Color
    let onSurfaceVariant: Color
    let outline: Color
    let error: Color
    let onError: Color
    let accentSun: Color

    static let light = DreamColors(
        primary: Color(red: 0x1F / 255, green: 0x5E / 255, blue: 0xFF / 255),
        onPrimary: Color.white,
        secondary: Color(red: 0x0F / 255, green: 0x76 / 255, blue: 0x6E / 255),
        onSecondary: Color.white,
        background: Color(red: 0xF4 / 255, green: 0xF7 / 255, blue: 0xFB / 255),
        surface: Color.white,
        surfaceVariant: Color(red: 0xE7 / 255, green: 0xED / 255, blue: 0xF6 / 255),
        onSurface: Color(red: 0x15 / 255, green: 0x20 / 255, blue: 0x33 / 255),
        onSurfaceVariant: Color(red: 0x51 / 255, green: 0x60 / 255, blue: 0x79 / 255),
        outline: Color(red: 0xB7 / 255, green: 0xC3 / 255, blue: 0xD7 / 255),
        error: Color(red: 0xB4 / 255, green: 0x23 / 255, blue: 0x18 / 255),
        onError: Color.white,
        accentSun: Color(red: 0xFF / 255, green: 0xB8 / 255, blue: 0x4D / 255)
    )

    static let dark = DreamColors(
        primary: Color(red: 0x8E / 255, green: 0xB0 / 255, blue: 0xFF / 255),
        onPrimary: Color(red: 0x03 / 255, green: 0x2B / 255, blue: 0x73 / 255),
        secondary: Color(red: 0x5E / 255, green: 0xD2 / 255, blue: 0xC3 / 255),
        onSecondary: Color(red: 0x00 / 255, green: 0x37 / 255, blue: 0x32 / 255),
        background: Color(red: 0x0B / 255, green: 0x12 / 255, blue: 0x20 / 255),
        surface: Color(red: 0x12 / 255, green: 0x1B / 255, blue: 0x2B / 255),
        surfaceVariant: Color(red: 0x22 / 255, green: 0x30 / 255, blue: 0x49 / 255),
        onSurface: Color(red: 0xE7 / 255, green: 0xED / 255, blue: 0xF8 / 255),
        onSurfaceVariant: Color(red: 0xAF / 255, green: 0xBD / 255, blue: 0xD4 / 255),
        outline: Color(red: 0x44 / 255, green: 0x53 / 255, blue: 0x6C / 255),
        error: Color(red: 0xFF / 255, green: 0xB4 / 255, blue: 0xAB / 255),
        onError: Color(red: 0x68 / 255, green: 0x00 / 255, blue: 0x03 / 255),
        accentSun: Color(red: 0xFF / 255, green: 0xB8 / 255, blue: 0x4D / 255)
    )
}


extension Font {
    static let dreamHeadlineLarge = Font.system(size: 36, weight: .bold)
    static let dreamHeadlineMedium = Font.system(size: 30, weight: .bold)
    static let dreamTitleLarge = Font.system(size: 22, weight: .semibold)
    static let dreamTitleMedium = Font.system(size: 18, weight: .semibold)
    static let dreamBodyLarge = Font.system(size: 16, weight: .regular)
    static let dreamBodyMedium = Font.system(size: 14, weight: .regular)
    static let dreamLabelLarge = Font.system(size: 14, weight: .medium)
}

private struct DreamColorsKey: EnvironmentKey {
    static let defaultValue = DreamColors.light
}

extension EnvironmentValues {
    var dreamColors: DreamColors {
        get { self[DreamColorsKey.self] }
        set { self[DreamColorsKey.self] = newValue }
    }
}

struct DreamThemeModifier: ViewModifier {
    @Environment(\.colorScheme) private var colorScheme

    func body(content: Content) -> some View {
        content.environment(\.dreamColors, colorScheme == .dark ? DreamColors.dark : DreamColors.light)
    }
}

extension View {
    func dreamTheme() -> some View {
        modifier(DreamThemeModifier())
    }
}

enum DreamButtonVariant {
    case primary
    case secondary
    case destructive
    case outlined
    case tertiary
}

enum DreamComponentSize {
    case small
    case medium
    case large
}
