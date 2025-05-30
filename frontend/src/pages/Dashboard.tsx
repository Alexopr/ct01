   // src/pages/Dashboard.tsx
   import React, { useState, useEffect } from 'react';
   import { getUsers } from '../services/userService';
   import { Button, Card, Alert, Skeleton, Chip } from '../components/ui';
   import { Icon } from '@iconify/react';
   
   const metrics = [
     { 
       label: 'Total Employees', 
       value: 12600, 
       icon: 'solar:users-group-rounded-bold',
       change: '+12%',
       changeType: 'positive' as const
     },
     { 
       label: 'Job Application', 
       value: 1186, 
       icon: 'solar:document-text-bold',
       change: '+8%',
       changeType: 'positive' as const
     },
     { 
       label: 'New Employees', 
       value: 22, 
       icon: 'solar:user-plus-bold',
       change: '+3',
       changeType: 'positive' as const
     },
     { 
       label: 'Satisfaction Rate', 
       value: '89.9%', 
       icon: 'solar:heart-bold',
       change: '+2.1%',
       changeType: 'positive' as const
     },
   ];
   
   interface Stats {
     totalUsers: number;
     adminUsers: number;
     regularUsers: number;
   }
   
   const Dashboard: React.FC = () => {
     const [stats, setStats] = useState<Stats>({
       totalUsers: 0,
       adminUsers: 0,
       regularUsers: 0
     });
     const [loading, setLoading] = useState(true);
     const [error, setError] = useState<string | null>(null);
     
     useEffect(() => {
       const fetchStats = async () => {
         setLoading(true);
         try {
           const users = await getUsers();
           const adminCount = users.filter(user => user.roles.includes('ADMIN')).length;
           
           setStats({
             totalUsers: users.length,
             adminUsers: adminCount,
             regularUsers: users.length - adminCount
           });
           setError(null);
         } catch (e: any) {
           setError(e.message || 'Ошибка загрузки статистики');
         }
         setLoading(false);
       };
       
       fetchStats();
     }, []);
     
     if (error) {
       return (
         <div className="p-6">
           <Alert
             type="error"
             title="Ошибка загрузки"
             description={error}
             variant="glass"
           />
         </div>
       );
     }
     
     return (
       <div className="min-h-screen bg-gradient-to-br from-background via-background/95 to-background/90 p-6 max-w-7xl mx-auto">
         <div className="space-y-8 animate-in fade-in-0 slide-in-from-bottom-4 duration-1000">
           
           {/* Header */}
           <div className="space-y-2">
             <h1 className="text-4xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
               Dashboard Overview
             </h1>
             <p className="text-foreground-600 text-lg">
               Welcome to your comprehensive analytics dashboard
             </p>
           </div>
           
           {/* Metrics Cards */}
           <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
             {metrics.map((metric, index) => (
               <Card
                 key={metric.label}
                 variant="glass"
                 hoverable
                 className="backdrop-blur-xl bg-background/30 border border-divider/20 shadow-xl transition-all duration-500 hover:shadow-2xl hover:shadow-primary/10 animate-in fade-in-0 slide-in-from-bottom-4"
                 style={{ animationDelay: `${index * 100}ms` }}
               >
                 <div className="p-6 space-y-4">
                   <div className="flex items-center justify-between">
                     <div className="p-3 rounded-xl bg-gradient-to-br from-primary/20 to-secondary/20 backdrop-blur-sm">
                       <Icon icon={metric.icon} className="w-6 h-6 text-primary" />
                     </div>
                     <Chip
                       variant="flat"
                       color={metric.changeType === 'positive' ? 'success' : 'danger'}
                       size="sm"
                       className="font-semibold"
                     >
                       {metric.change}
                     </Chip>
                   </div>
                   
                   <div className="space-y-1">
                     <p className="text-sm text-foreground-600 font-medium">
                       {metric.label}
                     </p>
                     <p className="text-3xl font-bold bg-gradient-to-r from-foreground to-foreground/80 bg-clip-text text-transparent">
                       {typeof metric.value === 'number' ? metric.value.toLocaleString() : metric.value}
                     </p>
                   </div>
                 </div>
               </Card>
             ))}
           </div>
           
           {/* Charts Section */}
           <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
             {/* Performance Chart */}
             <Card
               variant="glass"
               className="backdrop-blur-xl bg-background/30 border border-divider/20 shadow-xl animate-in fade-in-0 slide-in-from-left-4 duration-700"
               style={{ animationDelay: '400ms' }}
             >
               <div className="p-6 space-y-4">
                 <div className="flex items-center justify-between">
                   <div className="space-y-1">
                     <h3 className="text-xl font-semibold text-foreground">
                       Employees Performance
                     </h3>
                     <p className="text-sm text-foreground-600">
                       Monthly performance metrics
                     </p>
                   </div>
                   <Icon icon="solar:chart-bold" className="w-6 h-6 text-primary" />
                 </div>
                 
                 <div className="h-64 flex items-center justify-center">
                   {loading ? (
                     <Skeleton className="w-full h-full rounded-lg" />
                   ) : (
                     <div className="w-full h-full bg-gradient-to-br from-primary/5 to-secondary/5 rounded-lg flex items-center justify-center">
                       <div className="text-center space-y-2">
                         <Icon icon="solar:chart-2-bold" className="w-16 h-16 text-primary/40 mx-auto" />
                         <p className="text-foreground-500">Chart placeholder</p>
                       </div>
                     </div>
                   )}
                 </div>
               </div>
             </Card>
             
             {/* Attendance Chart */}
             <Card
               variant="glass"
               className="backdrop-blur-xl bg-background/30 border border-divider/20 shadow-xl animate-in fade-in-0 slide-in-from-right-4 duration-700"
               style={{ animationDelay: '500ms' }}
             >
               <div className="p-6 space-y-4">
                 <div className="flex items-center justify-between">
                   <div className="space-y-1">
                     <h3 className="text-xl font-semibold text-foreground">
                       Employee Attendance
                     </h3>
                     <p className="text-sm text-foreground-600">
                       Weekly attendance overview
                     </p>
                   </div>
                   <Icon icon="solar:clock-circle-bold" className="w-6 h-6 text-secondary" />
                 </div>
                 
                 <div className="h-64 flex items-center justify-center">
                   {loading ? (
                     <Skeleton className="w-48 h-48 rounded-full mx-auto" />
                   ) : (
                     <div className="w-48 h-48 bg-gradient-to-br from-secondary/5 to-primary/5 rounded-full flex items-center justify-center">
                       <div className="text-center space-y-2">
                         <Icon icon="solar:pie-chart-bold" className="w-16 h-16 text-secondary/40 mx-auto" />
                         <p className="text-foreground-500">Donut chart placeholder</p>
                       </div>
                     </div>
                   )}
                 </div>
               </div>
             </Card>
           </div>
           
           {/* Employee Table Section */}
           <Card
             variant="glass"
             className="backdrop-blur-xl bg-background/30 border border-divider/20 shadow-xl animate-in fade-in-0 slide-in-from-bottom-4 duration-700"
             style={{ animationDelay: '600ms' }}
           >
             <div className="p-6 space-y-6">
               <div className="flex items-center justify-between">
                 <div className="space-y-1">
                   <h3 className="text-xl font-semibold text-foreground">
                     Recent Employees
                   </h3>
                   <p className="text-sm text-foreground-600">
                     Overview of recent employee activities
                   </p>
                 </div>
                 <div className="flex items-center gap-2">
                   <Chip variant="flat" color="primary" size="sm">
                     {stats.totalUsers} Total
                   </Chip>
                   <Chip variant="flat" color="secondary" size="sm">
                     {stats.adminUsers} Admins
                   </Chip>
                 </div>
               </div>
               
               <div className="h-40">
                 {loading ? (
                   <div className="space-y-3">
                     {[...Array(3)].map((_, i) => (
                       <Skeleton key={i} className="w-full h-10 rounded-lg" />
                     ))}
                   </div>
                 ) : (
                   <div className="bg-gradient-to-br from-primary/5 to-secondary/5 rounded-lg h-full flex items-center justify-center">
                     <div className="text-center space-y-2">
                       <Icon icon="solar:users-group-rounded-bold" className="w-16 h-16 text-primary/40 mx-auto" />
                       <p className="text-foreground-500">Employee table placeholder</p>
                     </div>
                   </div>
                 )}
               </div>
             </div>
           </Card>
           
           {/* Action Buttons */}
           <div 
             className="flex flex-col sm:flex-row gap-4 animate-in fade-in-0 slide-in-from-bottom-4 duration-700"
             style={{ animationDelay: '700ms' }}
           >
             <Button
               variant="primary"
               size="lg"
               gradient
               icon="solar:eye-bold"
               className="transition-all duration-300 hover:shadow-xl hover:shadow-primary/25"
             >
               View Full Details
             </Button>
             
             <Button
               variant="ghost"
               size="lg"
               icon="solar:users-group-rounded-bold"
               className="border border-divider/30 hover:border-primary/50 transition-all duration-300"
             >
               All Employees
             </Button>
             
             <Button
               variant="secondary"
               size="lg"
               icon="solar:export-bold"
               className="transition-all duration-300 hover:shadow-lg hover:shadow-secondary/20"
             >
               Export Data
             </Button>
           </div>
         </div>
       </div>
     );
   };
   
   export default Dashboard;