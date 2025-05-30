import React, { useEffect, useState } from "react";
import { getUsers, deleteUser } from "../services/userService";
import type { User } from "../services/userService";
import { useNavigate, Link } from "react-router-dom";
import { DataGrid, GridActionsCellItem } from "@mui/x-data-grid";
import type { GridColDef, GridActionsColDef } from "@mui/x-data-grid";
import { Box, Paper, Typography, Button, Stack, Alert, Skeleton } from "@mui/material";

const UserList: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const data = await getUsers();
      setUsers(data);
      setError(null);
    } catch (e: any) {
      setError(e.message || "Ошибка загрузки пользователей");
    }
    setLoading(false);
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleDelete = async (id: number) => {
    if (!window.confirm("Удалить пользователя?")) return;
    try {
      await deleteUser(id);
      setUsers(users.filter(u => u.id !== id));
    } catch (e: any) {
      setError(e.message || "Ошибка удаления");
    }
  };

  const columns: (GridColDef | GridActionsColDef)[] = [
    { field: "id", headerName: "ID", width: 70 },
    { field: "username", headerName: "Username", width: 150 },
    { field: "email", headerName: "Email", width: 200 },
    { field: "roles", headerName: "Роли", width: 180, valueGetter: (params: any) => (params.row?.roles ? params.row.roles.join(", ") : "") },
    {
      field: "actions",
      type: 'actions',
      headerName: "Действия",
      width: 160,
      getActions: (params: any) => [
        <GridActionsCellItem label="Просмотр" onClick={() => navigate(`/users/${params.row.id}`)} showInMenu />,
        <GridActionsCellItem label="Редактировать" onClick={() => navigate(`/users/${params.row.id}/edit`)} showInMenu />,
        <GridActionsCellItem label="Удалить" onClick={() => handleDelete(params.row.id)} showInMenu />,
      ],
    },
  ];

  return (
    <Box sx={{ width: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '80vh', px: 2 }}>
      <Paper sx={{
        p: { xs: 2, sm: 4 },
        borderRadius: 6,
        boxShadow: '0 8px 40px 0 #6C47FF33, 0 2px 12px 0 #00E4FF22',
        bgcolor: 'rgba(35,38,58,0.7)',
        backdropFilter: 'blur(16px)',
        WebkitBackdropFilter: 'blur(16px)',
        width: '100%',
        maxWidth: 1100,
        animation: 'fadeIn 1s',
        '@keyframes fadeIn': {
          '0%': { opacity: 0, transform: 'translateY(32px)' },
          '100%': { opacity: 1, transform: 'none' },
        },
      }}>
        <Stack direction={{ xs: "column", sm: "row" }} justifyContent="space-between" alignItems={{ xs: "flex-start", sm: "center" }} mb={2} gap={2}>
          <Typography variant="h5" fontWeight={900} sx={{ color: 'primary.main', letterSpacing: 1.2, textShadow: '0 2px 16px #6C47FF33' }}>Пользователи</Typography>
          <Button
            variant="contained"
            color="primary"
            component={Link}
            to="/users/new"
            sx={{
              fontWeight: 700,
              fontSize: 18,
              borderRadius: 4,
              background: 'linear-gradient(135deg, #6C47FF 0%, #00E4FF 100%)',
              boxShadow: '0 4px 24px 0 #6C47FF33',
              transition: 'background 0.2s, box-shadow 0.2s',
              '&:hover': {
                background: 'linear-gradient(135deg, #00E4FF 0%, #6C47FF 100%)',
                boxShadow: '0 8px 32px 0 #00E4FF33',
              },
            }}
          >
            Добавить
          </Button>
        </Stack>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        {loading ? (
          <Skeleton variant="rectangular" width="100%" height={400} sx={{ borderRadius: 3 }} />
        ) : (
          <DataGrid
            rows={users}
            columns={columns}
            autoHeight
            pageSizeOptions={[10, 20, 50]}
            initialState={{ pagination: { paginationModel: { pageSize: 10 } } }}
            sx={{
              borderRadius: 3,
              bgcolor: 'rgba(35,38,58,0.5)',
              color: '#fff',
              minHeight: 320,
              fontSize: 16,
              boxShadow: '0 4px 24px 0 #6C47FF33',
              '& .MuiDataGrid-row': {
                transition: 'background 0.2s',
                '&:hover': {
                  background: 'rgba(108,71,255,0.08)',
                },
              },
              '& .MuiDataGrid-columnHeaders': {
                bgcolor: 'rgba(35,38,58,0.8)',
                color: 'primary.main',
                fontWeight: 800,
                fontSize: 18,
                borderBottom: '1px solid #6C47FF33',
              },
              '& .MuiDataGrid-footerContainer': {
                bgcolor: 'rgba(35,38,58,0.8)',
                borderTop: '1px solid #6C47FF33',
              },
              '& .MuiDataGrid-cell': {
                borderBottom: '1px solid #6C47FF22',
              },
              '& .MuiDataGrid-virtualScroller': {
                scrollbarColor: '#6C47FF #23263a',
                '&::-webkit-scrollbar': {
                  height: 8,
                  width: 8,
                  background: '#23263a',
                  borderRadius: 4,
                },
                '&::-webkit-scrollbar-thumb': {
                  background: 'linear-gradient(135deg, #6C47FF 0%, #00E4FF 100%)',
                  borderRadius: 4,
                },
              },
            }}
            disableRowSelectionOnClick
          />
        )}
      </Paper>
    </Box>
  );
};

export default UserList; 