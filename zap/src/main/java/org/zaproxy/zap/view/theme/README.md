# ZAP Modern Theme System

## Overview

This package contains the modern, professional theme system for ZAProxy with custom corporate-style themes.

## Available Themes

### 1. ZAP Turquoise (Light)
**Class:** `ZapTurquoiseTheme`
**Description:** A modern professional light theme with turquoise accents
**Color Palette:**
- Primary Accent: `#00A6A6` (Turquoise)
- Background: `#F5F8FA` (Light Gray)
- Text: `#1A1A1A` (Dark Gray)

**Features:**
- Fresh, corporate appearance
- Excellent readability
- Professional turquoise accents
- Clean, modern interface

### 2. ZAP Navy (Dark)
**Class:** `ZapNavyTheme`
**Description:** A modern professional dark theme with navy blue tones
**Color Palette:**
- Primary Accent: `#4A90E2` (Blue)
- Background: `#0F1B2D` (Navy Dark)
- Text: `#E8F1F5` (Light)
- Navy Tones: Various shades from `#0A1628` to `#2C5282`

**Features:**
- Elegant dark mode design
- Reduced eye strain
- Professional navy blue tones
- Modern corporate aesthetic

## Components

### ModernUIEnhancer
A utility class that applies modern styling to UI components:
- Enhanced menu bars with better spacing
- Modern button styling with rounded corners
- Improved toolbar appearance
- Better table and tree row heights
- Modern scrollbar styling

### Theme Properties Files
Each theme has an associated `.properties` file that defines:
- Color scheme
- Component-specific styling
- Borders and separators
- Selection colors
- Focus indicators

## Usage

Themes are automatically registered during startup via `ExtensionUiUtils`. Users can switch themes through:
1. **Toolbar:** Look & Feel selector button
2. **Menu:** Tools → Options → Display → Look and Feel

## Technical Details

### Architecture
- Extends FlatLAF (`FlatLightLaf` and `FlatDarkLaf`)
- Uses property files for color customization
- Applies modern UI defaults through `ModernUIEnhancer`
- Integrates seamlessly with ZAP's existing theme system

### Default Theme
The default theme is **ZAP Turquoise (Light)**, set in `OptionsParamView.java`.

### Dynamic Theme Switching
Themes can be changed at runtime without restarting ZAP. The UI automatically updates all components when a new theme is selected.

## Design Principles

1. **Professional Appearance:** Corporate-ready design suitable for security professionals
2. **Excellent Contrast:** Ensures readability for extended use
3. **Modern Aesthetics:** Clean lines, appropriate spacing, and modern visual elements
4. **Accessibility:** High contrast ratios and clear visual hierarchy
5. **Consistency:** Unified design language across all components

## Future Enhancements

Potential additions:
- Additional theme variants
- Theme customization options
- High contrast themes for accessibility
- Theme preview functionality
- Export/import custom themes

## Development

### Adding New Themes

1. Create a new theme class extending `FlatLightLaf` or `FlatDarkLaf`
2. Create a corresponding `.properties` file with color definitions
3. Register the theme in `ExtensionUiUtils.initView()`
4. Override `getDefaultsAsStream()` to load the properties file

### Example:
```java
public class MyCustomTheme extends FlatLightLaf {
    @Override
    public String getName() {
        return "My Custom Theme";
    }

    @Override
    public InputStream getDefaultsAsStream() {
        return MyCustomTheme.class.getResourceAsStream("MyCustomTheme.properties");
    }
}
```

## License

Copyright 2024 The ZAP Development Team
Licensed under the Apache License, Version 2.0
