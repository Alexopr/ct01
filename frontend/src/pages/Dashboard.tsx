   // src/pages/Dashboard.tsx
   import React, { useState, useEffect } from 'react';
   import { Paper, Typography, Box, Stack } from '@mui/material';
   import { getUsers } from '../services/userService';
   
   const Dashboard: React.FC = () => {
     const [stats, setStats] = useState({
       totalUsers: 0,
       adminUsers: 0,
       regularUsers: 0
     });
     
     useEffect(() => {
       const fetchStats = async () => {
         try {
           const users = await getUsers();
           const adminCount = users.filter(user => user.roles.includes('ADMIN')).length;
           
           setStats({
             totalUsers: users.length,
             adminUsers: adminCount,
             regularUsers: users.length - adminCount
           });
         } catch (error) {
           console.error('Failed to fetch statistics', error);
         }
       };
       
       fetchStats();
     }, []);
     
     return (
       <Box sx={{ width: '100%', mt: 2, mb: 2 }}>
         <Stack direction={{ xs: 'column', md: 'row' }} spacing={3}>
           <Paper sx={{ p: 2, flex: 1, display: 'flex', flexDirection: 'column', height: 140 }}>
             <Typography component="h2" variant="h6" color="primary" gutterBottom>
               Total Users
             </Typography>
             <Typography component="p" variant="h4">
               {stats.totalUsers}
             </Typography>
           </Paper>
           <Paper sx={{ p: 2, flex: 1, display: 'flex', flexDirection: 'column', height: 140 }}>
             <Typography component="h2" variant="h6" color="primary" gutterBottom>
               Admin Users
             </Typography>
             <Typography component="p" variant="h4">
               {stats.adminUsers}
             </Typography>
           </Paper>
           <Paper sx={{ p: 2, flex: 1, display: 'flex', flexDirection: 'column', height: 140 }}>
             <Typography component="h2" variant="h6" color="primary" gutterBottom>
               Regular Users
             </Typography>
             <Typography component="p" variant="h4">
               {stats.regularUsers}
             </Typography>
           </Paper>
         </Stack>
       </Box>
     );
   };
   
   export default Dashboard;