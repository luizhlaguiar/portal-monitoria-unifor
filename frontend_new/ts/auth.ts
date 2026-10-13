interface User {
    id: number;
    nome: string;
    role: string;
}

async function handleLogin(e: Event): Promise<void> {
    e.preventDefault();

    const matriculaInput = document.getElementById('matricula') as HTMLInputElement;
    const senhaInput = document.getElementById('senha') as HTMLInputElement;

    const matricula = matriculaInput.value;
    const senha = senhaInput.value;

    try {
        const response = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ matricula, senha })
        });

        if (response.ok) {
            const user: User = await response.json();
            localStorage.setItem('user', JSON.stringify(user));
            alert('Login realizado com sucesso! Bem-vindo, ' + user.nome);
            window.location.href = 'pages/dashboard.html';
        } else {
            alert('Erro ao fazer login: Credenciais inválidas');
        }
    } catch (error) {
        console.error('Erro de conexão:', error);
        alert('Erro ao conectar com o servidor. Certifique-se que o backend Java está rodando.');
    }
}

document.getElementById('loginForm')?.addEventListener('submit', handleLogin);
