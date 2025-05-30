import React from 'react';
import { Drawer, Box } from '@mui/material';

const SidebarDrawer: React.FC<{ open: boolean; onClose: () => void }> = ({ open, onClose }) => (
  <Drawer anchor="left" open={open} onClose={onClose}>
    <Box sx={{ width: 220, height: '100vh', bgcolor: 'rgba(35,38,58,0.98)', position: 'relative' }}>
      {/* Здесь расширенное меню */}
    </Box>
  </Drawer>
);

export default SidebarDrawer; 