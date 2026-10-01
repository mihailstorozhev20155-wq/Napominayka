// Инициализация данных
let reminders = JSON.parse(localStorage.getItem('reminders')) || [
    { id: 1, title: 'Прочитать книгу', date: '25.05.2025', time: '20:30', active: true },
    { id: 2, title: 'Купить продукты', date: '26.05.2025', time: '18:00', active: true },
    { id: 3, title: 'Тренировка', date: '27.05.2025', time: '07:00', active: true },
    { id: 4, title: 'Оплатить интернет', date: '27.05.2025', time: '12:00', active: false }
];

const listContainer = document.getElementById('reminders-list');
const screenHome = document.getElementById('screen-home');
const screenAdd = document.getElementById('screen-add');
const screenAlert = document.getElementById('screen-alert');

// Функция отрисовки списка
function renderReminders() {
    listContainer.innerHTML = '';
    reminders.forEach(rem => {
        const card = document.createElement('div');
        card.className = 'reminder-card';
        
        // Простая иконка в зависимости от текста
        let icon = '🔔';
        if (rem.title.toLowerCase().includes('книг')) icon = '📖';
        if (rem.title.toLowerCase().includes('продукт')) icon = '🛒';
        if (rem.title.toLowerCase().includes('трениров')) icon = '🏋️';
        if (rem.title.toLowerCase().includes('интернет')) icon = '💻';

        card.innerHTML = `
            <div class="card-info">
                <div class="card-title">${icon} ${rem.title}</div>
                <div class="card-meta">
                    <span>📅 ${rem.date}</span>
                    <span>🕒 ${rem.time}</span>
                </div>
            </div>
            <div class="card-actions">
                <label class="switch">
                    <input type="checkbox" ${rem.active ? 'checked' : ''} onchange="toggleReminder(${rem.id})">
                    <span class="slider"></span>
                </label>
                <button onclick="deleteReminder(${rem.id})" style="background:none;border:none;font-size:18px;">🗑️</button>
            </div>
        `;
        listContainer.appendChild(card);
    });
}

// Переключение состояния
function toggleReminder(id) {
    reminders = reminders.map(r => r.id === id ? {...r, active: !r.active} : r);
    saveAndRender();
}

// Удаление
function deleteReminder(id) {
    reminders = reminders.filter(r => r.id !== id);
    saveAndRender();
}

function saveAndRender() {
    localStorage.setItem('reminders', JSON.stringify(reminders));
    renderReminders();
}

// Навигация
document.getElementById('add-btn').addEventListener('click', () => {
    screenHome.classList.remove('active');
    screenAdd.classList.add('active');
    // Устанавливаем текущую дату
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('reminder-date').value = today;
});

document.getElementById('back-btn').addEventListener('click', () => {
    screenAdd.classList.remove('active');
    screenHome.classList.add('active');
});

document.getElementById('cancel-btn').addEventListener('click', () => {
    screenAdd.classList.remove('active');
    screenHome.classList.add('active');
});

// Сохранение нового напоминания
document.getElementById('save-btn').addEventListener('click', () => {
    const text = document.getElementById('reminder-text').value;
    const date = document.getElementById('reminder-date').value;
    const time = document.getElementById('reminder-time').value;

    if (!text || !date || !time) {
        alert('Пожалуйста, заполните все поля!');
        return;
    }

    // Форматируем дату для отображения
    const [year, month, day] = date.split('-');
    const formattedDate = `${day}.${month}.${year}`;

    const newReminder = {
        id: Date.now(),
        title: text,
        date: formattedDate,
        time: time,
        active: true
    };

    reminders.push(newReminder);
    saveAndRender();

    // Очищаем поля и возвращаемся
    document.getElementById('reminder-text').value = '';
    screenAdd.classList.remove('active');
    screenHome.classList.add('active');

    // Демонстрация уведомления (Экран 3)
    showAlert(text);
});

// Показ уведомления
function showAlert(text) {
    document.getElementById('alert-text').textContent = text;
    screenAlert.classList.add('active');
}

document.getElementById('alert-ok-btn').addEventListener('click', () => {
    screenAlert.classList.remove('active');
});

// Первоначальная отрисовка
renderReminders();
