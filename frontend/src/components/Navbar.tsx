import React, { useState } from 'react';
import { AppBar, Toolbar, Typography, Button, IconButton, Drawer, List, ListItem, ListItemText, Box, useMediaQuery, useTheme } from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Navbar: React.FC = () => {
  const [drawerOpen, setDrawerOpen] = useState(false);
  const { isAuthenticated, isAdmin, logout } = useAuth();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  const toggleDrawer = (open: boolean) => () => {
    setDrawerOpen(open);
  };

  const navItems = [
    { text: 'Home', path: '/' },
    ...(isAuthenticated() ? [
      { text: 'Profile', path: '/profile' },
      ...(isAdmin() ? [{ text: 'Admin', path: '/admin' }] : [])
    ] : [
      { text: 'Login', path: '/login' },
      { text: 'Register', path: '/register' }
    ])
  ];

  const drawer = (
    <Box onClick={toggleDrawer(false)} sx={{ width: 220 }}>
      <List>
        {navItems.map((item) => (
          <ListItem component={Link} to={item.path} key={item.text} sx={{ cursor: 'pointer' }}>
            <ListItemText primary={item.text} />
          </ListItem>
        ))}
        {isAuthenticated() && (
          <ListItem component="button" onClick={logout} key="logout" sx={{ cursor: 'pointer' }}>
            <ListItemText primary="Logout" />
          </ListItem>
        )}
      </List>
    </Box>
  );

  return (
    <AppBar position="static">
      <Toolbar>
        <Typography variant="h6" component={Link} to="/" sx={{ flexGrow: 1, textDecoration: 'none', color: 'inherit' }}>
          CRUD Admin
        </Typography>
        {isMobile ? (
          <>
            <IconButton
              edge="end"
              color="inherit"
              aria-label="menu"
              onClick={toggleDrawer(true)}
            >
              <MenuIcon />
            </IconButton>
            <Drawer anchor="right" open={drawerOpen} onClose={toggleDrawer(false)}>
              {drawer}
            </Drawer>
          </>
        ) : (
          <Box>
            {navItems.map((item) => (
              <Button color="inherit" component={Link} to={item.path} key={item.text}>
                {item.text}
              </Button>
            ))}
            {isAuthenticated() && (
              <Button color="inherit" onClick={logout}>Logout</Button>
            )}
          </Box>
        )}
      </Toolbar>
    </AppBar>
  );
};

export default Navbar; 