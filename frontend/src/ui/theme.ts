// ui/theme: the one MUI theme. Every value comes from ./tokens/tokens (the Figma export); the
// choices of which token goes where: black primary, status colours for status only, no ripple,
// square corners. Anything the tokens do not define stays at MUI default. Only ui/ uses MUI;
// features import components from @/ui.
import { createTheme } from '@mui/material/styles';
import * as tokens from './tokens/tokens';

type Level =
  | 'Level2'
  | 'Level4'
  | 'Level5'
  | 'Level6'
  | 'Level8Regular'
  | 'Level9Regular'
  | 'Level11Light'
  | 'Level11Regular'
  | 'Level12Light'
  | 'Level12Regular'
  | 'Level13Regular';

const level = (name: Level) => ({
  fontFamily: tokens[`Typography${name}FontFamily`],
  fontWeight: tokens[`Typography${name}FontWeight`],
  fontSize: tokens[`Typography${name}FontSize`],
  lineHeight: tokens[`Typography${name}LineHeight`],
  letterSpacing: tokens[`Typography${name}LetterSpacing`],
});

export const theme = createTheme({
  palette: {
    primary: {
      main: tokens.ColorGrayscaleBlack,
      light: tokens.ColorGrayscaleBlack,
      dark: tokens.ColorGrayscaleBlack,
    },
    secondary: {
      main: tokens.ColorGrayscaleBlack,
      light: tokens.ColorGrayscaleBlack,
      dark: tokens.ColorGrayscaleBlack,
    },
    error: { main: tokens.ColorStatusRed },
    warning: { main: tokens.ColorStatusOrange },
    info: { main: tokens.ColorStatusBlue },
    success: { main: tokens.ColorStatusGreen },
    text: {
      primary: tokens.ColorGrayscaleBlack,
      secondary: tokens.ColorGrayscaleGrey,
      disabled: tokens.ColorGrayscaleGrey,
    },
    background: { default: tokens.ColorGrayscaleWhite, paper: tokens.ColorGrayscaleWhite },
    divider: tokens.ColorGrayscaleSoftGrey,
    action: { hover: tokens.ColorGrayscaleLightGrey, selected: tokens.ColorGrayscaleSoftGrey },
  },
  // The Figma spacing scale steps in 5px: theme.spacing(2) is 10px, theme.spacing(4) is 20px.
  spacing: Number.parseInt(tokens.Space5, 10),
  shape: { borderRadius: 0 },
  // MUI variant → Figma text style: h4 is the page title; sizes are the nearest Figma level.
  // TODO(design): confirm against the usage notes on the Figma Typography page (docs/open-points.md #20).
  typography: {
    fontFamily: tokens.FontFamilyBase,
    fontWeightLight: tokens.TypographyLevel11LightFontWeight,
    fontWeightRegular: tokens.TypographyLevel11RegularFontWeight,
    fontWeightMedium: tokens.TypographyLevel11SemiboldFontWeight,
    fontWeightBold: tokens.TypographyLevel11SemiboldFontWeight,
    h1: level('Level2'),
    h2: level('Level4'),
    h3: level('Level5'),
    h4: level('Level6'),
    h5: level('Level8Regular'),
    h6: level('Level9Regular'),
    subtitle1: level('Level11Regular'),
    subtitle2: level('Level12Regular'),
    body1: level('Level11Light'),
    body2: level('Level12Light'),
    button: { ...level('Level12Regular'), textTransform: 'uppercase' },
    caption: level('Level13Regular'),
    overline: { ...level('Level13Regular'), textTransform: 'uppercase' },
  },
  components: {
    MuiButtonBase: { defaultProps: { disableRipple: true } },
    MuiTableHead: { styleOverrides: { root: { backgroundColor: tokens.ColorAccentArcticBase } } },
    MuiMenuItem: {
      styleOverrides: {
        root: {
          '&.Mui-selected, &.Mui-selected:hover': { backgroundColor: tokens.ColorAccentArcticBase },
        },
      },
    },
  },
});
