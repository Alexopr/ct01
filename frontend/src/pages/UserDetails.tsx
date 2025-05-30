import React, { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { getUserById } from "../services/userService";
import type { User } from "../services/userService";
import { Box, Paper, Typography, Stack, Button, Alert, Skeleton } from "@mui/material";

const UserDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    getUserById(Number(id))
      .then(setUser)
      .catch(e => setError(e.message || "Ошибка загрузки"))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <Skeleton variant="rectangular" width="100%" height={200} sx={{ borderRadius: 4, mt: 4 }} />;
  if (error) return <Alert severity="error" sx={{ mt: 4 }}>{error}</Alert>;
  if (!user) return <Alert severity="warning" sx={{ mt: 4 }}>Пользователь не найден</Alert>;

  return (
    <Box
      sx={{
        minHeight: '80vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        px: 2,
        py: 8,
        background: 'linear-gradient(135deg, #23263a 0%, #2d314d 100%)',
        position: 'relative',
        overflow: 'hidden',
        animation: 'fadeIn 1s',
        '@keyframes fadeIn': {
          '0%': { opacity: 0, transform: 'translateY(32px)' },
          '100%': { opacity: 1, transform: 'none' },
        },
      }}
    >
      <Paper sx={{
        p: { xs: 2, sm: 6 },
        borderRadius: 6,
        boxShadow: '0 8px 40px 0 #6C47FF33, 0 2px 12px 0 #00E4FF22',
        bgcolor: 'rgba(35,38,58,0.7)',
        backdropFilter: 'blur(16px)',
        WebkitBackdropFilter: 'blur(16px)',
        maxWidth: 500,
        width: '100%',
        textAlign: 'center',
      }}>
        <Typography variant="h4" fontWeight={900} sx={{ color: 'primary.main', letterSpacing: 1.2, mb: 3, textShadow: '0 2px 16px #6C47FF33' }}>
          Пользователь: {user.username}
        </Typography>
        <Stack spacing={2} mb={4}>
          <Typography fontSize={18} color="text.secondary">Email: <b style={{ color: '#fff' }}>{user.email}</b></Typography>
          <Typography fontSize={18} color="text.secondary">Роли: <b style={{ color: '#fff' }}>{user.roles.join(", ")}</b></Typography>
          {user.createdAt && <Typography fontSize={16} color="text.secondary">Создан: <b style={{ color: '#fff' }}>{user.createdAt}</b></Typography>}
          {user.updatedAt && <Typography fontSize={16} color="text.secondary">Обновлён: <b style={{ color: '#fff' }}>{user.updatedAt}</b></Typography>}
        </Stack>
        <Stack direction="row" spacing={2} justifyContent="center">
          <Button
            variant="contained"
            color="primary"
            component={Link}
            to={`/users/${user.id}/edit`}
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
            Редактировать
          </Button>
          <Button variant="outlined" color="secondary" component={Link} to="/users" sx={{ fontWeight: 700, borderRadius: 4 }}>
            Назад
          </Button>
        </Stack>
      </Paper>
    </Box>
  );
};

export default UserDetails; 