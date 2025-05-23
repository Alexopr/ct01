import React, { useState } from "react";
import { Box, Drawer, List, ListItem, ListItemIcon, ListItemText, Toolbar, Typography, Paper, Divider, IconButton, useTheme, useMediaQuery, Stack } from "@mui/material";
import DashboardIcon from '@mui/icons-material/Dashboard';
import PeopleIcon from '@mui/icons-material/People';
import SettingsIcon from '@mui/icons-material/Settings';
import MenuIcon from '@mui/icons-material/Menu';
import Dashboard from "./Dashboard";
import UserManagement from "./UserManagement";

const drawerWidth = 220;

const SidebarContent = ({ onNavigate }: { onNavigate?: () => void }) => (
  <Box sx={{ mt: 2 }}>
    <List>
      <ListItem component="button" onClick={onNavigate}>
        <ListItemIcon sx={{ color: '#fff' }}><DashboardIcon /></ListItemIcon>
        <ListItemText primary="Dashboard" />
      </ListItem>
      <ListItem component="button" onClick={onNavigate}>
        <ListItemIcon sx={{ color: '#fff' }}><PeopleIcon /></ListItemIcon>
        <ListItemText primary="Users" />
      </ListItem>
      <ListItem component="button" onClick={onNavigate}>
        <ListItemIcon sx={{ color: '#fff' }}><SettingsIcon /></ListItemIcon>
        <ListItemText primary="Settings" />
      </ListItem>
    </List>
  </Box>
);

const StatWidget = () => (
  <Paper sx={{ p: 3, borderRadius: 3, boxShadow: 2, height: '100%' }}>
    <Typography variant="subtitle2" color="#5E6278" gutterBottom>
      Quick Stats
    </Typography>
    <Divider sx={{ mb: 2 }} />
    <Typography variant="body2" color="#5E6278">Active users: <b>1</b></Typography>
    <Typography variant="body2" color="#5E6278">Admins: <b>1</b></Typography>
    <Typography variant="body2" color="#5E6278">Regular: <b>0</b></Typography>
  </Paper>
);

const AdminPanel: React.FC = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const [drawerOpen, setDrawerOpen] = useState(false);

  const handleDrawerToggle = () => setDrawerOpen(!drawerOpen);

  return (
    <Box sx={{ display: 'flex', bgcolor: '#F4F6F8', minHeight: '100vh' }}>
      {/* Sidebar for desktop */}
      {!isMobile && (
        <Drawer
          variant="permanent"
          sx={{
            width: drawerWidth,
            flexShrink: 0,
            [`& .MuiDrawer-paper`]: {
              width: drawerWidth,
              boxSizing: 'border-box',
              bgcolor: '#181C32',
              color: '#fff',
              borderRight: 0
            },
          }}
          open
        >
          <Toolbar />
          <SidebarContent />
        </Drawer>
      )}
      {/* Drawer for mobile */}
      {isMobile && (
        <Drawer
          anchor="left"
          open={drawerOpen}
          onClose={handleDrawerToggle}
          sx={{
            [`& .MuiDrawer-paper`]: {
              width: drawerWidth,
              bgcolor: '#181C32',
              color: '#fff',
              borderRight: 0
            },
          }}
        >
          <Toolbar />
          <SidebarContent onNavigate={handleDrawerToggle} />
        </Drawer>
      )}
      <Box component="main" sx={{ flexGrow: 1, p: { xs: 2, sm: 4 }, ml: { sm: `${drawerWidth}px` } }}>
        {isMobile && (
          <IconButton onClick={handleDrawerToggle} sx={{ mb: 2 }}>
            <MenuIcon />
          </IconButton>
        )}
        <Typography
          variant="h4"
          sx={{ fontWeight: 700, mb: 3, fontSize: { xs: '1.5rem', sm: '2rem', md: '2.5rem' }, color: '#181C32' }}
        >
          Admin Panel
        </Typography>
        <Stack direction={{ xs: 'column', md: 'row' }} spacing={3} sx={{ mb: 3 }}>
          <Box sx={{ flex: 1, minWidth: 0 }}>
            <Dashboard />
          </Box>
          <Box sx={{ width: { xs: '100%', md: 300 }, minWidth: 220 }}>
            <StatWidget />
          </Box>
        </Stack>
        <Paper sx={{ p: { xs: 1, sm: 3 }, borderRadius: 3, boxShadow: 2 }}>
          <UserManagement />
        </Paper>
      </Box>
    </Box>
  );
};

export default AdminPanel;