import { createTheme } from '@mui/material/styles';
import { blueGrey, cyan, amber, red, green } from '@mui/material/colors';

const GRADIENT_PRIMARY = 'linear-gradient(135deg, #6C47FF 0%, #00E4FF 100%)';
export const GRADIENT_CARD = 'linear-gradient(135deg, #23263a 60%, #2d314d 100%)';
const GLASS_BG = 'rgba(35,38,58,0.7)';
const GLOW_SHADOW = '0 4px 32px 0 rgba(108,71,255,0.25), 0 1.5px 8px 0 rgba(0,228,255,0.10)';

// Вынести glow отдельно, если нужно использовать в sx
export const GLOW_COLOR = '#6C47FF';

const SHADOWS: [
  "none",
  string, string, string, string, string, string, string, string, string, string, string, string, string, string, string, string, string, string, string, string, string, string, string, string
] = [
  'none',
  '0 2px 8px 0 rgba(0,0,0,0.12)',
  '0 4px 32px 0 rgba(108,71,255,0.15)',
  GLOW_SHADOW,
  '0 2px 16px 0 rgba(0,0,0,0.25)',
  '0 2px 16px 0 rgba(0,0,0,0.25)',
  '0 2px 16px 0 rgba(0,0,0,0.25)',
  '0 2px 16px 0 rgba(0,0,0,0.25)',
  '0 2px 16px 0 rgba(0,0,0,0.25)',
  '0 2px 16px 0 rgba(0,0,0,0.25)',
  '0 2px 16px 0 rgba(0,0,0,0.25)',
  '0 2px 16px 0 rgba(0,0,0,0.25)',
  '0 2px 16px 0 rgba(0,0,0,0.25)',
  '0 2px 16px 0 rgba(0,0,0,0.25)',
  '0 2px 16px 0 rgba(0,0,0,0.25)',
  '0 2px 16px 0 rgba(0,0,0,0.25)',
  '0 2px 16px 0 rgba(0,0,0,0.25)',
  '0 2px 16px 0 rgba(0,0,0,0.25)',
  '0 2px 16px 0 rgba(0,0,0,0.25)',
  '0 2px 16px 0 rgba(0,0,0,0.25)',
  '0 2px 16px 0 rgba(0,0,0,0.25)',
  '0 2px 16px 0 rgba(0,0,0,0.25)',
  '0 2px 16px 0 rgba(0,0,0,0.25)',
  '0 2px 16px 0 rgba(0,0,0,0.25)',
  '0 2px 16px 0 rgba(0,0,0,0.25)'
];

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#6C47FF',
      light: '#9B7CFF',
      dark: '#3A1C8B',
      contrastText: '#fff',
    },
    secondary: {
      main: '#00E4FF',
      light: '#5CFFF8',
      dark: '#0096A6',
      contrastText: '#fff',
    },
    error: {
      main: red[400],
    },
    success: {
      main: green[400],
    },
    warning: {
      main: amber[400],
    },
    info: {
      main: cyan[400],
    },
    background: {
      default: '#181A20',
      paper: '#23263a',
    },
    text: {
      primary: '#fff',
      secondary: blueGrey[200],
      disabled: '#7A7A8C',
    },
    divider: 'rgba(255,255,255,0.08)',
  },
  shape: {
    borderRadius: 20,
  },
  shadows: SHADOWS,
  typography: {
    fontFamily: 'Inter, Roboto, Arial, sans-serif',
    h1: { fontWeight: 800, fontSize: '2.8rem', letterSpacing: -1 },
    h2: { fontWeight: 700, fontSize: '2.2rem' },
    h3: { fontWeight: 700, fontSize: '1.8rem' },
    h4: { fontWeight: 700, fontSize: '1.4rem' },
    h5: { fontWeight: 700, fontSize: '1.1rem' },
    h6: { fontWeight: 700, fontSize: '1rem' },
    button: { textTransform: 'none', fontWeight: 600, letterSpacing: 0.2 },
    body1: { fontWeight: 400, fontSize: '1rem' },
    body2: { fontWeight: 400, fontSize: '0.95rem' },
    subtitle1: { fontWeight: 500, fontSize: '1.05rem' },
    subtitle2: { fontWeight: 500, fontSize: '0.95rem' },
  },
  components: {
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
          backgroundColor: '#23263a',
          backdropFilter: 'blur(12px)',
          boxShadow: GLOW_SHADOW,
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          background: 'rgba(24,26,32,0.85)',
          boxShadow: '0 2px 16px 0 rgba(0,0,0,0.25)',
          backdropFilter: 'blur(8px)',
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          background: GLASS_BG,
          borderRight: '1px solid #23263a',
          backdropFilter: 'blur(16px)',
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          background: GRADIENT_PRIMARY,
          boxShadow: '0 2px 8px 0 rgba(108,71,255,0.10)',
          color: '#fff',
          '&:hover': {
            background: 'linear-gradient(135deg, #00E4FF 0%, #6C47FF 100%)',
            boxShadow: '0 4px 16px 0 rgba(0,228,255,0.15)',
          },
        },
      },
    },
  },
});

export default theme; 