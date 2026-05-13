import React from 'react';
import Editor from '@monaco-editor/react';
import {
  BrowserRouter,
  Link,
  Navigate,
  Route,
  Routes,
  useNavigate,
  useParams
} from 'react-router-dom';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8081/api';
const APP_NAME = 'CODE QUEST AI';

const languageOptions = [
  { value: 'java', label: 'Java' },
  { value: 'python', label: 'Python' },
  { value: 'javascript', label: 'JavaScript' },
  { value: 'typescript', label: 'TypeScript' },
  { value: 'cpp', label: 'C++' },
  { value: 'c', label: 'C' },
  { value: 'go', label: 'Go' },
  { value: 'rust', label: 'Rust' }
];

const AuthContext = React.createContext(null);

function App() {
  const [theme, setTheme] = React.useState(() => window.localStorage.getItem('theme-preference') || 'light');

  React.useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    window.localStorage.setItem('theme-preference', theme);
  }, [theme]);

  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="app-shell">
          <div className="ambient ambient-left" />
          <div className="ambient ambient-right" />
          <Routes>
            <Route path="/" element={<LandingPage theme={theme} onToggleTheme={() => toggleTheme(setTheme)} />} />
            <Route path="/login" element={<LoginPage theme={theme} onToggleTheme={() => toggleTheme(setTheme)} />} />
            <Route path="/signup" element={<SignupPage theme={theme} onToggleTheme={() => toggleTheme(setTheme)} />} />
            <Route path="/oauth/callback" element={<OAuthCallbackPage />} />
            <Route
              path="/dashboard"
              element={<ProtectedRoute><DashboardPage theme={theme} onToggleTheme={() => toggleTheme(setTheme)} /></ProtectedRoute>}
            />
            <Route
              path="/learn"
              element={<ProtectedRoute><LearningTracksPage theme={theme} onToggleTheme={() => toggleTheme(setTheme)} /></ProtectedRoute>}
            />
            <Route
              path="/repo-scan"
              element={<ProtectedRoute><RepoScanPage theme={theme} onToggleTheme={() => toggleTheme(setTheme)} /></ProtectedRoute>}
            />
            <Route
              path="/track/:id"
              element={<ProtectedRoute><TrackPage theme={theme} onToggleTheme={() => toggleTheme(setTheme)} /></ProtectedRoute>}
            />
            <Route
              path="/problem/:id"
              element={<ProtectedRoute><ProblemPage theme={theme} onToggleTheme={() => toggleTheme(setTheme)} /></ProtectedRoute>}
            />
          </Routes>
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
}

function AuthProvider({ children }) {
  const [token, setToken] = React.useState(() => window.localStorage.getItem('auth-token'));
  const [user, setUser] = React.useState(() => readStoredUser());
  const [isBootstrapping, setIsBootstrapping] = React.useState(true);

  const saveSession = React.useCallback((authResponse) => {
    const accessToken = authResponse.accessToken || authResponse.token;
    setToken(accessToken);
    setUser(authResponse.user);
    window.localStorage.setItem('auth-token', accessToken);
    window.localStorage.setItem('auth-user', JSON.stringify(authResponse.user));
  }, []);

  const clearSession = React.useCallback(() => {
    setToken(null);
    setUser(null);
    clearStoredSession();
  }, []);

  React.useEffect(() => {
    if (token && user) {
      setIsBootstrapping(false);
      return;
    }

    refreshSession()
      .then((authResponse) => {
        if (authResponse?.accessToken || authResponse?.token) {
          saveSession(authResponse);
        }
      })
      .catch(() => {
        clearSession();
      })
      .finally(() => setIsBootstrapping(false));
  }, [clearSession, saveSession, token, user]);

  async function logout() {
    await request('/auth/logout', { method: 'POST', token }).catch(() => null);
    clearSession();
  }

  return (
    <AuthContext.Provider value={{ token, user, saveSession, setUser, logout, isBootstrapping }}>
      {children}
    </AuthContext.Provider>
  );
}

function useAuth() {
  return React.useContext(AuthContext);
}

function ProtectedRoute({ children }) {
  const { token, isBootstrapping } = useAuth();
  if (isBootstrapping) {
    return <FullPageLoader label="Restoring session" />;
  }
  return token ? children : <Navigate to="/login" replace />;
}

function LandingPage({ theme, onToggleTheme }) {
  const { token } = useAuth();

  return (
    <main className="landing-page cq-landing">
      <nav className="landing-nav">
        <Link className="landing-logo" to="/">
          <span>CQ</span>
          {APP_NAME}
        </Link>
        <div className="landing-nav-links">
          <a href="#features">Features</a>
          <a href="#workflow">Workflow</a>
          <a href="#product">Product</a>
          <a href="#pricing">Start</a>
        </div>
        <div className="landing-nav-actions">
          <ThemeToggle theme={theme} onToggle={onToggleTheme} />
          <Link className="ghost-button" to={token ? '/dashboard' : '/login'}>{token ? 'Dashboard' : 'Login'}</Link>
          <Link className="primary-button" to={token ? '/dashboard' : '/signup'}>{token ? 'Open app' : 'Start free'}</Link>
        </div>
      </nav>

      <section className="cq-hero">
        <div className="cq-hero-scene" aria-hidden="true">
          <div className="code-window hero-code-window">
            <div className="window-dots"><span /><span /><span /></div>
            <pre>{`class Quest {
  solve(problem) {
    const insight = ai.review(problem);
    return submit(insight.fix());
  }
}`}</pre>
          </div>
          <div className="hero-orbit-card orbit-one">
            <strong>91</strong>
            <span>DSA quests</span>
          </div>
          <div className="hero-orbit-card orbit-two">
            <strong>AI</strong>
            <span>repo review</span>
          </div>
        </div>
        <div className="cq-hero-content">
          <p className="hero-pill">AI coding workspace for ambitious developers</p>
          <h1>Master DSA. Review real code. Level up with AI.</h1>
          <p>
            Code Quest AI combines a LeetCode-style practice path with repo scanning,
            failure explanations, suggested fixes, and learning resources in one focused workspace.
          </p>
          <div className="cq-hero-actions">
            <Link className="primary-button" to={token ? '/dashboard' : '/signup'}>Start your quest</Link>
            <Link className="ghost-button" to="/login">Continue with Google</Link>
          </div>
          <div className="cq-hero-metrics">
            <span><strong>15</strong> DSA tracks</span>
            <span><strong>90+</strong> problems</span>
            <span><strong>2</strong> learning modes</span>
          </div>
        </div>
      </section>

      <section className="trusted-strip">
        {['DSA practice', 'AI feedback', 'Repo scan', 'OAuth login', 'XP streaks'].map((item) => <span key={item}>{item}</span>)}
      </section>

      <section className="landing-section product-section" id="product">
        <div className="section-intro product-intro">
          <p className="section-kicker">Product preview</p>
          <h2>One command center for practice and code review.</h2>
          <p>Switch from structured interview prep to real repository analysis without leaving your learning flow.</p>
          <div className="section-stat-strip">
            <span><strong>2</strong> modes</span>
            <span><strong>90+</strong> quests</span>
            <span><strong>AI</strong> review</span>
          </div>
        </div>
        <div className="product-mockup">
          <div className="mock-sidebar">
            <strong>Code Quest AI</strong>
            <span className="active">Dashboard</span>
            <span>Learning Track</span>
            <span>Repo Scan</span>
            <span>Leaderboard</span>
          </div>
          <div className="mock-main">
            <div className="mock-header">
              <div>
                <small>Workspace</small>
                <h3>Choose your next move</h3>
              </div>
              <span>Live</span>
            </div>
            <div className="mock-command-bar">
              <span>AI</span>
              <strong>Review my failing Two Sum case</strong>
              <em>ready in context</em>
            </div>
            <div className="mock-grid">
              <div className="mock-card large">
                <p>Learning Track</p>
                <strong>Arrays, Graphs, DP, Greedy</strong>
                <div className="mock-progress"><span style={{ width: '68%' }} /></div>
                <div className="mock-track-list">
                  <span>Binary Search</span>
                  <span>Recursion</span>
                  <span>Heaps</span>
                </div>
              </div>
              <div className="mock-card">
                <p>Repo Scan</p>
                <strong>12 findings</strong>
                <div className="mock-stack-bars"><i /><i /><i /><i /></div>
              </div>
              <div className="mock-card">
                <p>Feedback</p>
                <strong>Fix + examples</strong>
                <div className="mock-feedback-chips"><span>edge case</span><span>code</span></div>
              </div>
            </div>
            <div className="mock-code">
              <span>AI explanation</span>
              <pre>{`Use a hash map to store complements.
Failed case: [3,2,4], target 6
Suggested fix: return [1,2]`}</pre>
            </div>
          </div>
        </div>
      </section>

      <section className="landing-section" id="features">
        <div className="section-intro">
          <p className="section-kicker">Features</p>
          <h2>Built for serious practice, not passive tutorials.</h2>
        </div>
        <div className="premium-feature-grid">
          {[ 
            ['Guided DSA tracks', 'Arrays to DP, ordered like a serious interview roadmap with progress built in.'],
            ['Failure-aware feedback', 'Wrong answers get structured examples, edge cases, suggested code, and learning resources.'],
            ['Repository scanner', 'Upload a source file or zip and receive bug reports, fixes, and testing ideas.'],
            ['Modern auth', 'Email login plus Google and GitHub OAuth with refresh-token sessions.'],
            ['Gamified momentum', 'XP, streaks, levels, rank, and solved counters keep progress visible.'],
            ['Multi-language editor', 'Practice with Java, Python, JavaScript, TypeScript, C++, Go, Rust, and more.']
          ].map(([title, copy], index) => (
            <article className={`premium-feature-card feature-card-${index + 1}`} key={title}>
              <div className="feature-card-top">
                <span>{String(index + 1).padStart(2, '0')}</span>
                <div className="feature-spark" aria-hidden="true"><i /><i /><i /></div>
              </div>
              <h3>{title}</h3>
              <p>{copy}</p>
              <div className="feature-signal">
                <small>{['Roadmap', 'Debug', 'Scan', 'Session', 'Momentum', 'Editor'][index]}</small>
                <em />
              </div>
            </article>
          ))}
        </div>
      </section>

      <section className="landing-section workflow-section" id="workflow">
        <div className="section-intro centered">
          <p className="section-kicker">Workflow</p>
          <h2>From stuck to shipping in three moves.</h2>
        </div>
        <div className="workflow-grid">
          <div><span>01</span><div className="workflow-visual"><i /><i /><i /></div><h3>Pick a quest</h3><p>Choose Learning Track for DSA or Repo Scan for project review.</p></div>
          <div><span>02</span><div className="workflow-visual code"><i /><i /><i /></div><h3>Submit code</h3><p>Run your solution and inspect failing cases without losing context.</p></div>
          <div><span>03</span><div className="workflow-visual done"><i /><i /><i /></div><h3>Learn the fix</h3><p>Use explanations, examples, and resources to understand the next move.</p></div>
        </div>
      </section>

      <section className="landing-section split-section">
        <div className="split-copy">
          <p className="section-kicker">AI review</p>
          <h2>Turn repository scans into clear engineering feedback.</h2>
          <p>Code Quest AI reads uploaded source files or repo archives and returns bugs, fixes, edge cases, complexity notes, and resources.</p>
          <Link className="primary-button" to={token ? '/repo-scan' : '/signup'}>Scan a repo</Link>
        </div>
        <div className="review-panel">
          <div className="review-panel-head"><span>Scan summary</span><strong>4 checks</strong></div>
          <div className="review-row accepted"><strong>Security</strong><span>Input validation added</span></div>
          <div className="review-row warning"><strong>Performance</strong><span>Nested loop can be reduced</span></div>
          <div className="review-row"><strong>Tests</strong><span>Missing empty-array case</span></div>
          <div className="review-code-diff">
            <code>+ if (nums == null) return new int[] {'{'}-1, -1{'}'};</code>
            <code>- brute force every pair</code>
            <code>+ store complements in a hash map</code>
          </div>
        </div>
      </section>

      <section className="landing-section testimonial-section premium-testimonials">
        <div className="section-intro centered">
          <p className="section-kicker">Where it helps</p>
          <h2>Designed for students who want product-level polish.</h2>
        </div>
        <div className="testimonial-grid">
          <article className="testimonial-card proof-card">
            <span>Practice mode</span>
            <strong>Wrong answers become a lesson.</strong>
            <p>Failed cases, expected output, actual behavior, hints, and suggested code stay grouped together.</p>
          </article>
          <article className="testimonial-card proof-card">
            <span>Repo mode</span>
            <strong>Projects get review notes.</strong>
            <p>Upload source files or a zip and turn raw code into bugs, fixes, test ideas, and complexity notes.</p>
          </article>
          <article className="testimonial-card proof-card">
            <span>Momentum</span>
            <strong>Progress is visible every day.</strong>
            <p>Tracks, XP, streaks, rank, and solved counters keep the dashboard focused on your next move.</p>
          </article>
        </div>
      </section>

      <section className="landing-section final-cta" id="pricing">
        <p className="section-kicker">Start now</p>
        <h2>Build your coding momentum today.</h2>
        <p>Practice with structure, debug with AI, and keep moving.</p>
        <div className="cq-hero-actions">
          <Link className="primary-button" to={token ? '/dashboard' : '/signup'}>Launch Code Quest AI</Link>
          <Link className="ghost-button" to="/login">Sign in</Link>
        </div>
      </section>
    </main>
  );
}

function LoginPage({ theme, onToggleTheme }) {
  const [email, setEmail] = React.useState('');
  const [password, setPassword] = React.useState('');
  const [rememberMe, setRememberMe] = React.useState(true);
  const [showPassword, setShowPassword] = React.useState(false);
  const [toast, setToast] = React.useState(null);
  const [isLoading, setIsLoading] = React.useState(false);
  const { token, saveSession } = useAuth();
  const navigate = useNavigate();

  if (token) {
    return <Navigate to="/dashboard" replace />;
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setIsLoading(true);
    setToast(null);

    try {
      const normalizedEmail = email.trim();
      const data = await request('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email: normalizedEmail, password, rememberMe })
      });
      saveSession(data);
      setToast({ type: 'success', message: 'Welcome back.' });
      navigate('/dashboard');
    } catch (requestError) {
      setToast({ type: 'error', message: requestError.message });
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <AuthShell
      title="Welcome back"
      subtitle="Continue learning with guided feedback, XP, and streaks."
      theme={theme}
      onToggleTheme={onToggleTheme}
      toast={toast}
    >
      <OAuthButtons />
      <Divider />
      <form className="auth-form modern-auth-form" onSubmit={handleSubmit} autoComplete="off">
        <AuthInput label="Email" value={email} onChange={setEmail} placeholder="you@example.com" type="email" autoComplete="off" required />
        <PasswordInput
          label="Password"
          value={password}
          onChange={setPassword}
          showPassword={showPassword}
          onToggle={() => setShowPassword((current) => !current)}
          autoComplete="new-password"
          required
        />
        <div className="auth-row">
          <label className="check-row">
            <input type="checkbox" checked={rememberMe} onChange={(event) => setRememberMe(event.target.checked)} />
            <span>Remember me</span>
          </label>
          <a href="mailto:support@codequestai.dev">Forgot password?</a>
        </div>
        <button className="primary-button auth-submit" type="submit" disabled={isLoading}>
          {isLoading ? <Spinner label="Signing in" /> : 'Login'}
        </button>
      </form>
      <p className="auth-switch">No account yet? <Link to="/signup">Create one</Link></p>
    </AuthShell>
  );
}

function SignupPage({ theme, onToggleTheme }) {
  const [name, setName] = React.useState('');
  const [email, setEmail] = React.useState('');
  const [password, setPassword] = React.useState('');
  const [confirmPassword, setConfirmPassword] = React.useState('');
  const [showPassword, setShowPassword] = React.useState(false);
  const [toast, setToast] = React.useState(null);
  const [isLoading, setIsLoading] = React.useState(false);
  const { token, saveSession } = useAuth();
  const navigate = useNavigate();
  const strength = passwordStrength(password);

  if (token) {
    return <Navigate to="/dashboard" replace />;
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setToast(null);

    if (password !== confirmPassword) {
      setToast({ type: 'error', message: 'Passwords do not match.' });
      return;
    }

    if (password.length < 8) {
      setToast({ type: 'error', message: 'Password must be at least 8 characters.' });
      return;
    }

    setIsLoading(true);
    try {
      const data = await request('/auth/signup', {
        method: 'POST',
        body: JSON.stringify({
          name: name.trim(),
          email: email.trim(),
          password,
          confirmPassword,
          rememberMe: true
        })
      });
      saveSession(data);
      setToast({ type: 'success', message: 'Account created.' });
      navigate('/dashboard');
    } catch (requestError) {
      setToast({ type: 'error', message: requestError.message });
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <AuthShell
      title="Create your account"
      subtitle="Start solving curated tracks with smart feedback."
      theme={theme}
      onToggleTheme={onToggleTheme}
      toast={toast}
    >
      <OAuthButtons />
      <Divider />
      <form className="auth-form modern-auth-form" onSubmit={handleSubmit}>
        <AuthInput label="Name" value={name} onChange={setName} placeholder="Your name" autoComplete="name" required />
        <AuthInput label="Email" value={email} onChange={setEmail} placeholder="you@example.com" type="email" autoComplete="email" required />
        <PasswordInput
          label="Password"
          value={password}
          onChange={setPassword}
          showPassword={showPassword}
          onToggle={() => setShowPassword((current) => !current)}
          autoComplete="new-password"
          minLength={8}
          required
        />
        <PasswordStrength strength={strength} />
        <PasswordInput
          label="Confirm password"
          value={confirmPassword}
          onChange={setConfirmPassword}
          showPassword={showPassword}
          onToggle={() => setShowPassword((current) => !current)}
          autoComplete="new-password"
          minLength={8}
          required
        />
        <button className="primary-button auth-submit" type="submit" disabled={isLoading}>
          {isLoading ? <Spinner label="Creating" /> : 'Sign Up'}
        </button>
      </form>
      <p className="auth-switch">Already have an account? <Link to="/login">Login</Link></p>
    </AuthShell>
  );
}

function OAuthCallbackPage() {
  const { saveSession } = useAuth();
  const navigate = useNavigate();

  React.useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const oauthToken = params.get('token');
    const status = params.get('status');

    async function completeLogin() {
      try {
        if (status !== 'success') {
          throw new Error('Social login was cancelled or failed.');
        }

        if (oauthToken) {
          const user = await request('/auth/me', { token: oauthToken });
          saveSession({ accessToken: oauthToken, user });
        } else {
          const authResponse = await refreshSession();
          saveSession(authResponse);
        }

        window.history.replaceState({}, document.title, '/oauth/callback');
        navigate('/dashboard', { replace: true });
      } catch {
        clearStoredSession();
        navigate('/login', { replace: true });
      }
    }

    completeLogin();
  }, [navigate, saveSession]);

  return <FullPageLoader label="Completing social login" />;
}

function AuthShell({ title, subtitle, theme, onToggleTheme, toast, children }) {
  return (
    <main className="auth-page modern-auth-page">
      <section className="auth-showcase">
        <div className="auth-background-mesh" aria-hidden="true">
          <span />
          <span />
          <span />
        </div>
        <div className="auth-showcase-copy">
          <div className="auth-brand-pill">{APP_NAME}</div>
          <h1>Log in to your AI coding command center.</h1>
          <p>Practice DSA, scan repositories, inspect failing cases, and keep your progress moving from one premium workspace.</p>
          <div className="auth-mini-grid">
            <span>Smart feedback</span>
            <span>Repo scanner</span>
            <span>Progress engine</span>
          </div>
        </div>
        <div className="auth-visual-panel" aria-hidden="true">
          <div className="auth-visual-header">
            <span>Live workspace</span>
            <strong>Code Quest AI</strong>
          </div>
          <div className="auth-editor-preview">
            <div className="window-dots"><span /><span /><span /></div>
            <pre>{`function twoSum(nums, target) {
  const seen = new Map();
  for (let i = 0; i < nums.length; i++) {
    const need = target - nums[i];
    if (seen.has(need)) return [seen.get(need), i];
    seen.set(nums[i], i);
  }
}`}</pre>
          </div>
          <div className="auth-insight-row">
            <div className="auth-progress-ring"><strong>68%</strong><span>track</span></div>
            <div className="auth-code-card">
              <span>AI feedback</span>
              <pre>{`Failed case: [3,2,4], target 6
Hint: store complements
Fix: return [1,2]`}</pre>
            </div>
          </div>
          <div className="auth-visual-grid">
            <span><strong>15</strong> tracks</span>
            <span><strong>90+</strong> quests</span>
            <span><strong>2</strong> modes</span>
          </div>
          <div className="auth-activity-line">
            <span>Arrays</span>
            <i />
            <span>Submit</span>
            <i />
            <span>AI fix</span>
          </div>
        </div>
      </section>
      <section className="auth-panel auth-card-modern">
        <div className="auth-card-glow" aria-hidden="true" />
        <div className="auth-card-header">
          <div>
            <p className="eyebrow">Secure access</p>
            <h2>{title}</h2>
            <p>{subtitle}</p>
          </div>
          <ThemeToggle theme={theme} onToggle={onToggleTheme} />
        </div>
        {toast ? <Toast type={toast.type} message={toast.message} /> : null}
        {children}
      </section>
    </main>
  );
}

function OAuthButtons() {
  return (
    <div className="oauth-grid">
      <OAuthButton provider="google" icon="G" label="Continue with Google" />
      <OAuthButton provider="github" icon="GH" label="Continue with GitHub" />
    </div>
  );
}

function OAuthButton({ provider, icon, label }) {
  async function startOAuth() {
    clearStoredSession();
    await request('/auth/logout', { method: 'POST' }).catch(() => null);
    window.location.href = `${API_BASE_URL.replace('/api', '')}/oauth2/authorization/${provider}`;
  }

  return (
    <button className={`oauth-button ${provider}`} type="button" onClick={startOAuth}>
      <span>{icon}</span>
      {label}
    </button>
  );
}

function Divider() {
  return <div className="auth-divider"><span /> <em>or</em> <span /></div>;
}

function AuthInput({ label, value, onChange, placeholder, type = 'text', autoComplete, required }) {
  return (
    <label className="field auth-field">
      <span>{label}</span>
      <input
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        type={type}
        autoComplete={autoComplete}
        required={required}
      />
    </label>
  );
}

function PasswordInput({ label, value, onChange, showPassword, onToggle, autoComplete, minLength, required }) {
  return (
    <label className="field auth-field">
      <span>{label}</span>
      <div className="password-field">
        <input
          value={value}
          onChange={(event) => onChange(event.target.value)}
          placeholder="At least 8 characters"
          type={showPassword ? 'text' : 'password'}
          autoComplete={autoComplete}
          minLength={minLength}
          required={required}
        />
        <button type="button" onClick={onToggle}>{showPassword ? 'Hide' : 'Show'}</button>
      </div>
    </label>
  );
}

function PasswordStrength({ strength }) {
  return (
    <div className="password-strength">
      <div className="strength-track">
        <span style={{ width: `${strength.score * 25}%` }} />
      </div>
      <small>{strength.label}</small>
    </div>
  );
}

function Toast({ type, message }) {
  return <div className={`toast ${type}`}>{message}</div>;
}

function Spinner({ label }) {
  return <span className="spinner-label"><span className="spinner" />{label}</span>;
}

function FullPageLoader({ label }) {
  return (
    <main className="auth-page">
      <div className="auth-panel glass-card loader-panel">
        <Spinner label={label} />
      </div>
    </main>
  );
}

function DashboardPage({ theme, onToggleTheme }) {
  const { user, token, logout } = useAuth();
  const [tracks, setTracks] = React.useState([]);
  const [myRank, setMyRank] = React.useState(null);
  const [error, setError] = React.useState('');

  React.useEffect(() => {
    request('/tracks', { token })
      .then(setTracks)
      .catch((requestError) => setError(requestError.message));

    request('/leaderboard/me', { token })
      .then(setMyRank)
      .catch(() => setMyRank(null));
  }, [token]);

  const solvedTotal = tracks.reduce((sum, track) => sum + (track.solvedProblems || 0), 0);
  const problemTotal = tracks.reduce((sum, track) => sum + (track.totalProblems || 0), 0);
  const completion = problemTotal ? Math.round((solvedTotal / problemTotal) * 100) : 0;
  const availableTracks = tracks.length;
  const displayName = user?.name || 'Coder';
  const nextTrack = tracks.find((track) => (track.solvedProblems || 0) < (track.totalProblems || 0)) || tracks[0];
  const nextTrackName = nextTrack?.name || 'Arrays';

  return (
    <div className="workspace-page dashboard-command-page">
      <AppTopbar title="Dashboard" theme={theme} onToggleTheme={onToggleTheme} onLogout={logout} />

      <section className="dashboard-command-grid">
        <div className="dashboard-home-hero dashboard-command-hero glass-card">
          <div className="dashboard-orbit-bg" aria-hidden="true"><span /><span /><span /></div>
          <div className="home-hero-copy">
            <p className="eyebrow">Workspace</p>
            <h2>Welcome, {displayName}. Build skill, review code, move faster.</h2>
            <p className="hero-text">Your AI coding cockpit for DSA practice, repo scanning, failed-case feedback, and daily coding momentum.</p>
            <div className="dashboard-hero-actions">
              <Link className="primary-button" to="/learn">Start learning</Link>
              <Link className="ghost-button hero-ghost-button" to="/repo-scan">Scan repo</Link>
            </div>
          </div>
          <div className="dashboard-live-preview" aria-hidden="true">
            <div className="dashboard-preview-top">
              <span>AI review</span>
              <strong>Live</strong>
            </div>
            <pre>{`case: [3,2,4], target 6
hint: use a complement map
fix: return [1,2]`}</pre>
            <div className="dashboard-preview-pills">
              <span>Edge case</span>
              <span>Complexity</span>
              <span>Fix ready</span>
            </div>
          </div>
        </div>

        <div className="home-hero-panel dashboard-progress-panel">
          <div className="home-progress-orbit" style={{ '--progress': `${completion * 3.6}deg` }}>
            <strong>{completion}%</strong>
            <span>DSA progress</span>
          </div>
          <div className="home-mini-stats">
            <MetricCard label="Tracks" value={availableTracks} />
            <MetricCard label="Solved" value={`${solvedTotal}/${problemTotal || 0}`} />
          </div>
          <div className="dashboard-progress-meta">
            <span>Next focus</span>
            <strong>{nextTrackName}</strong>
          </div>
        </div>
      </section>

      {error ? <p className="error-banner">{error}</p> : null}

      <section className="mode-choice-grid dashboard-action-grid">
        <Link className="mode-choice-card learning" to="/learn">
          <div className="choice-card-head">
            <span className="choice-kicker">Learning</span>
            <em>{completion}% complete</em>
          </div>
          <h2>Learning Track</h2>
          <p>Practice ordered DSA tracks, solve problems, build XP, and unlock structured feedback when a submission fails.</p>
          <div className="choice-visual learning-visual" aria-hidden="true">
            <span style={{ height: '38%' }} />
            <span style={{ height: '64%' }} />
            <span style={{ height: '82%' }} />
            <span style={{ height: '48%' }} />
          </div>
          <strong>{solvedTotal}/{problemTotal || 0} solved</strong>
        </Link>
        <Link className="mode-choice-card repo" to="/repo-scan">
          <div className="choice-card-head">
            <span className="choice-kicker">Review</span>
            <em>AI scanner</em>
          </div>
          <h2>Repo Scan</h2>
          <p>Upload a repository zip or source file and get structured AI review with suggested fixes and test ideas.</p>
          <div className="choice-visual repo-visual" aria-hidden="true">
            <span>Security <em>input validation</em></span>
            <span>Tests <em>missing empty case</em></span>
            <span>Fix <em>patch ready</em></span>
          </div>
          <strong>Upload source or .zip</strong>
        </Link>
      </section>

      <section className="dashboard-info-grid dashboard-bento-grid">
        <div className="panel dashboard-info-card dashboard-flow-card">
          <p className="section-kicker">Today</p>
          <h3>Recommended coding flow</h3>
          <div className="info-steps">
            <span>1</span>
            <p>Warm up with one DSA problem from Learning Track.</p>
            <span>2</span>
            <p>Submit your solution and review feedback only if it fails.</p>
            <span>3</span>
            <p>Scan a repo/file when you want a practical code review pass.</p>
          </div>
        </div>
        <div className="panel dashboard-info-card dashboard-signal-card">
          <p className="section-kicker">System</p>
          <h3>AI workspace signals</h3>
          <div className="signal-list">
            <span><strong>Practice engine</strong><em>Ready</em></span>
            <span><strong>Repo scanner</strong><em>Online</em></span>
            <span><strong>Feedback loop</strong><em>Active</em></span>
          </div>
        </div>
        <div className="panel dashboard-info-card profile-card dashboard-profile-card">
          <div className="profile-inline">
            <div className="profile-avatar">{initials(displayName)}</div>
            <div>
              <p className="section-kicker">Your profile</p>
              <h3>Welcome, {displayName}</h3>
              <span>@{slugify(displayName)}</span>
            </div>
          </div>
          <div className="profile-rank">
            <span>Rank</span>
            <strong>{myRank?.rank ? `#${myRank.rank.toLocaleString('en-IN')}` : 'Calculating'}</strong>
          </div>
          <div className="momentum-grid">
            <MetricCard label="XP" value={user?.xp ?? 0} />
            <MetricCard label="Level" value={user?.level ?? 0} />
            <MetricCard label="Streak" value={`${user?.streak ?? 0} day(s)`} />
          </div>
          <div className="profile-energy-line"><span style={{ width: `${Math.max(8, completion)}%` }} /></div>
        </div>
      </section>
    </div>
  );
}

function initials(name) {
  return String(name || 'CQ')
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join('') || 'CQ';
}

function slugify(name) {
  return String(name || 'coder')
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-|-$/g, '') || 'coder';
}

function LearningTracksPage({ theme, onToggleTheme }) {
  const { user, token, logout } = useAuth();
  const [tracks, setTracks] = React.useState([]);
  const [leaderboard, setLeaderboard] = React.useState([]);
  const [searchTerm, setSearchTerm] = React.useState('');
  const [error, setError] = React.useState('');

  React.useEffect(() => {
    request('/tracks', { token })
      .then(setTracks)
      .catch((requestError) => setError(requestError.message));

    request('/leaderboard', { token })
      .then(setLeaderboard)
      .catch(() => setLeaderboard([]));
  }, [token]);

  const solvedTotal = tracks.reduce((sum, track) => sum + (track.solvedProblems || 0), 0);
  const problemTotal = tracks.reduce((sum, track) => sum + (track.totalProblems || 0), 0);
  const completion = problemTotal ? Math.round((solvedTotal / problemTotal) * 100) : 0;
  const activeTrack = tracks.find((track) => (track.solvedProblems || 0) < (track.totalProblems || 0)) || tracks[0];
  const normalizedSearch = searchTerm.trim().toLowerCase();
  const filteredTracks = normalizedSearch
          ? tracks.filter((track) => `${track.name} ${track.description}`.toLowerCase().includes(normalizedSearch))
          : tracks;

  return (
    <div className="workspace-page">
      <AppTopbar
        title="Learning Track"
        theme={theme}
        onToggleTheme={onToggleTheme}
        onLogout={logout}
        searchValue={searchTerm}
        onSearchChange={setSearchTerm}
        searchPlaceholder="Search tracks..."
      />
      <section className="dashboard-hero dashboard-hero-modern glass-card">
        <div className="dashboard-hero-copy">
          <p className="eyebrow">Learning track</p>
          <h2>Learning Track</h2>
          <p className="hero-text">Practice ordered tracks, clear problems, build XP, and keep your streak alive.</p>
          <div className="dashboard-hero-actions">
            {activeTrack ? <Link className="primary-button" to={`/track/${activeTrack.id}`}>Continue quest</Link> : null}
            <span className="quest-chip">{completion}% overall complete</span>
          </div>
        </div>
        <div className="dashboard-stat-grid">
          <QuestStat label="XP" value={user?.xp ?? 0} tone="blue" />
          <QuestStat label="Level" value={user?.level ?? 0} tone="teal" />
          <QuestStat label="Streak" value={`${user?.streak ?? 0} day(s)`} tone="gold" />
          <QuestStat label="Solved" value={`${solvedTotal}/${problemTotal || 0}`} tone="rose" />
        </div>
      </section>

      <XpProgress user={user} />
      <section className="dashboard-part">
        <div className="part-heading">
          <div>
            <p className="section-kicker">Learning Track</p>
            <h2>Tracks</h2>
          </div>
          <Link className="ghost-button" to="/dashboard">Dashboard</Link>
        </div>
        {error ? <p className="error-banner">{error}</p> : null}
        <main className="dashboard-board">
          <aside className="panel sheet-side-panel">
            <p className="section-kicker">Tracks</p>
            <h2>Tracks</h2>
            <div className="sheet-track-list">
              {filteredTracks.map((track) => {
                const solved = track.solvedProblems || 0;
                const total = track.totalProblems || 0;
                const progress = total ? Math.round((solved / total) * 100) : 0;
                return (
                  <Link className="sheet-track-link" to={`/track/${track.id}`} key={`side-${track.id}`}>
                    <span>{track.name}</span>
                    <em>{progress}%</em>
                  </Link>
                );
              })}
              {filteredTracks.length === 0 ? <p className="muted-copy">No tracks match your search.</p> : null}
            </div>
          </aside>

          <section className="panel dashboard-panel tracks-panel-modern">
            <div className="section-heading">
              <div>
                <p className="section-kicker">Tracks</p>
                <h2>Choose your next challenge</h2>
              </div>
              <span className="quest-chip">{filteredTracks.length}/{tracks.length} paths</span>
            </div>
            <div className="track-grid">
              {filteredTracks.map((track) => (
                <TrackQuestCard track={track} key={track.id} />
              ))}
            </div>
          </section>

          <section className="panel leaderboard-panel leaderboard-panel-modern">
            <p className="section-kicker">Leaderboard</p>
            <h2>Top learners</h2>
            <div className="leaderboard-list">
              {leaderboard.map((entry) => (
                <div className="leaderboard-row leaderboard-row-modern" key={`${entry.rank}-${entry.name}`}>
                  <span className="rank-badge">#{entry.rank}</span>
                  <strong>{entry.name}</strong>
                  <em>{entry.xp} XP</em>
                </div>
              ))}
              {leaderboard.length === 0 ? <p className="muted-copy">No learners yet. Submit a solution to claim the board.</p> : null}
            </div>
          </section>
        </main>
      </section>
    </div>
  );
}

function RepoScanPage({ theme, onToggleTheme }) {
  const { token, logout } = useAuth();

  return (
    <div className="workspace-page">
      <AppTopbar title="Repo Scan" theme={theme} onToggleTheme={onToggleTheme} onLogout={logout} />
      <section className="workspace-header track-hero glass-card">
        <div className="track-hero-copy">
          <p className="eyebrow">Repository scanner</p>
          <h2>Scan a codebase</h2>
          <p className="hero-text workspace-text">Upload a repository archive or source file to find bugs, suggested fixes, edge cases, and learning resources.</p>
          <div className="dashboard-hero-actions">
            <span className="quest-chip">Zip or source file</span>
            <span className="quest-chip">AI review</span>
          </div>
        </div>
        <Link className="ghost-button" to="/dashboard">Dashboard</Link>
      </section>
      <RepoUploadPanel token={token} />
    </div>
  );
}

function RepoUploadPanel({ token }) {
  const [language, setLanguage] = React.useState('');
  const [file, setFile] = React.useState(null);
  const [result, setResult] = React.useState(null);
  const [error, setError] = React.useState('');
  const [isScanning, setIsScanning] = React.useState(false);

  async function scanUpload(event) {
    event.preventDefault();
    if (!file) {
      setError('Choose a source file or repository zip first.');
      return;
    }

    setIsScanning(true);
    setError('');
    setResult(null);

    const formData = new FormData();
    formData.append('file', file);
    if (language) {
      formData.append('language', language);
    }

    try {
      const data = await request('/analyze/upload', {
        method: 'POST',
        token,
        body: formData
      });
      setResult(data);
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setIsScanning(false);
    }
  }

  return (
    <section className="repo-scan-grid">
      <form className="panel repo-upload-panel" onSubmit={scanUpload}>
        <div>
          <p className="section-kicker">Scan codebase</p>
          <h3>Upload a repository or source file</h3>
          <p className="upload-subtitle">Drop in a `.zip` repository archive or a single code file to get bugs, fixes, edge cases, and learning notes.</p>
        </div>

        <label className="upload-zone repo-upload-zone">
          <input
            type="file"
            accept=".zip,.java,.py,.js,.jsx,.ts,.tsx,.cpp,.c,.go,.rs,.cs,.php,.rb,.kt,.swift"
            onChange={(event) => setFile(event.target.files?.[0] || null)}
          />
          <span className="upload-title">{file ? file.name : 'Choose file or repository zip'}</span>
          <small>{file ? `${Math.max(1, Math.round(file.size / 1024))} KB selected` : 'Supported: zip, java, python, javascript, typescript, c++, go, rust'}</small>
        </label>

        <label className="field">
          <span>Language hint</span>
          <select value={language} onChange={(event) => setLanguage(event.target.value)}>
            <option value="">Auto detect</option>
            {languageOptions.map((option) => (
              <option value={option.value} key={option.value}>{option.label}</option>
            ))}
          </select>
        </label>

        {error ? <p className="error-banner">{error}</p> : null}
        <button className="primary-button" type="submit" disabled={isScanning}>
          {isScanning ? <Spinner label="Scanning" /> : 'Scan repository'}
        </button>
      </form>

      <section className="panel repo-result-panel">
        {result ? <AnalysisResult result={result} /> : (
          <div className="empty-state compact-empty">
            <h3>No scan yet</h3>
            <p>Upload a repo zip or source file to see structured AI review, suggested fixes, and test ideas here.</p>
          </div>
        )}
      </section>
    </section>
  );
}

function AnalysisResult({ result }) {
  return (
    <div className="feedback-grid repo-feedback-grid">
      <ResultCard title="Bugs Found" items={result.bugs} accent="rose" />
      <ExplanationCard content={result.explanation} />
      <CodeCard title="Suggested Fix" code={result.fixedCode} />
      <ResultCard title="Edge Cases To Test" items={result.edgeCasesToTest} accent="teal" />
      <LearningResources resources={result.learningResources} />
      <div className="metrics-grid">
        <MetricCard label="Time" value={result.timeComplexity || '-'} />
        <MetricCard label="Space" value={result.spaceComplexity || '-'} />
        <MetricCard label="Optimality" value={result.optimality || '-'} />
      </div>
    </div>
  );
}

function QuestStat({ label, value, tone }) {
  return (
    <div className={`quest-stat ${tone}`}>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function TrackQuestCard({ track }) {
  const solved = track.solvedProblems || 0;
  const total = track.totalProblems || 0;
  const progress = total ? Math.round((solved / total) * 100) : 0;

  return (
    <Link className="track-card track-card-modern" to={`/track/${track.id}`}>
      <div className="track-card-top">
        <span className="track-label">Track</span>
        <strong>{progress}%</strong>
      </div>
      <h3>{track.name}</h3>
      <p>{track.description}</p>
      <div className="track-progress" aria-label={`${progress}% complete`}>
        <span style={{ width: `${progress}%` }} />
      </div>
      <div className="track-meta-row">
        <span>{solved}/{total} solved</span>
        <em>Open</em>
      </div>
    </Link>
  );
}

function TrackPage({ theme, onToggleTheme }) {
  const { id } = useParams();
  const { token, logout } = useAuth();
  const [problems, setProblems] = React.useState([]);
  const [tracks, setTracks] = React.useState([]);
  const [error, setError] = React.useState('');

  React.useEffect(() => {
    Promise.all([
      request(`/problems?trackId=${id}`, { token }),
      request('/tracks', { token })
    ])
      .then(([problemData, trackData]) => {
        setProblems(problemData);
        setTracks(trackData);
      })
      .catch((requestError) => setError(requestError.message));
  }, [id, token]);

  const track = tracks.find((item) => String(item.id) === String(id));
  const solvedCount = problems.filter((problem) => problem.solved).length;
  const progress = problems.length ? Math.round((solvedCount / problems.length) * 100) : 0;

  return (
    <div className="workspace-page">
      <AppTopbar title={track?.name || 'Track'} theme={theme} onToggleTheme={onToggleTheme} onLogout={logout} />
      <section className="workspace-header track-hero glass-card">
        <div className="track-hero-copy">
          <p className="eyebrow">Learning track</p>
          <h2>{track?.name || 'Problems'}</h2>
          <p className="hero-text workspace-text">{track?.description || 'Work through this set of coding problems.'}</p>
          <div className="dashboard-hero-actions">
            <span className="quest-chip">{problems.length} questions</span>
            <span className="quest-chip">{solvedCount} solved</span>
          </div>
        </div>
        <div className="track-progress-ring" style={{ '--progress': `${progress * 3.6}deg` }}>
          <strong>{progress}%</strong>
          <span>Progress</span>
        </div>
      </section>
      {error ? <p className="error-banner">{error}</p> : null}
      <section className="panel question-sheet-panel">
        <div className="question-sheet-head">
          <div>
            <h2>{track?.name || 'Track'} Questions</h2>
            <p>{solvedCount}/{problems.length} completed</p>
          </div>
          <Link className="ghost-button" to="/dashboard">Dashboard</Link>
        </div>
        <div className="question-table">
          <div className="question-table-row question-table-header">
            <span>Status</span>
            <span>Problem</span>
            <span>Practice</span>
            <span>Level</span>
            <span>Timer</span>
          </div>
          {problems.map((problem, index) => (
            <Link className="question-table-row" to={`/problem/${problem.id}`} key={problem.id}>
              <span className={`status-dot ${problem.solved ? 'solved' : ''}`}>{problem.solved ? '✓' : index + 1}</span>
              <strong>{problem.title}</strong>
              <span className="practice-pill">Code</span>
              <span className={`difficulty ${problem.difficulty.toLowerCase()}`}>{problem.difficulty}</span>
              <span className="timer-pill">{problem.difficulty === 'HARD' ? '50 Min' : problem.difficulty === 'MEDIUM' ? '40 Min' : '30 Min'}</span>
            </Link>
          ))}
        </div>
      </section>
    </div>
  );
}

function ProblemPage({ theme, onToggleTheme }) {
  const { id } = useParams();
  const { token, setUser, logout } = useAuth();
  const [problem, setProblem] = React.useState(null);
  const [language, setLanguage] = React.useState('java');
  const [code, setCode] = React.useState('');
  const [result, setResult] = React.useState(null);
  const [error, setError] = React.useState('');
  const [isLoading, setIsLoading] = React.useState(false);

  React.useEffect(() => {
    request(`/problems/${id}`, { token })
      .then((data) => {
        setProblem(data);
        setCode(data.starterCode || '');
      })
      .catch((requestError) => setError(requestError.message));
  }, [id, token]);

  async function submitCode() {
    setIsLoading(true);
    setError('');
    setResult(null);

    try {
      const data = await request('/submit', {
        method: 'POST',
        token,
        body: JSON.stringify({ problemId: Number(id), language, code })
      });
      setResult(data);
      setUser((currentUser) => currentUser ? {
        ...currentUser,
        xp: data.totalXp,
        level: data.level,
        streak: data.streak
      } : currentUser);
      window.localStorage.setItem('auth-user', JSON.stringify({
        ...readStoredUser(),
        xp: data.totalXp,
        level: data.level,
        streak: data.streak
      }));
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setIsLoading(false);
    }
  }

  const examples = parseProblemExamples(problem?.testCases);
  const hints = problemHints(problem);

  return (
    <div className="workspace-page">
      <AppTopbar title="Problem" theme={theme} onToggleTheme={onToggleTheme} onLogout={logout} />
      <main className="problem-layout">
        <section className="panel problem-statement">
          <div className="statement-topline">
            <Link className="ghost-button statement-back" to={problem ? `/track/${problem.trackId}` : '/dashboard'}>Back</Link>
            <p className="section-kicker">{problem?.trackName || 'Track'}</p>
          </div>
          <div className="statement-title-row">
            <h2>{problem?.title || 'Loading problem...'}</h2>
            {problem ? <span className={`difficulty ${problem.difficulty.toLowerCase()}`}>{problem.difficulty}</span> : null}
          </div>
          <p className="statement-description">{problem?.description}</p>

          <section className="statement-section">
            <h3>Examples</h3>
            <div className="example-list">
              {examples.map((example, index) => (
                <div className="leetcode-example" key={`${example.input}-${index}`}>
                  <strong>Example {index + 1}</strong>
                  <pre>{`Input: ${example.input}\nOutput: ${example.expectedOutput}`}</pre>
                </div>
              ))}
            </div>
          </section>

          <section className="statement-section">
            <h3>Constraints</h3>
            <ul className="constraint-list">
              <li>Inputs follow the format shown in the examples.</li>
              <li>Return exactly the expected output shape.</li>
              <li>Think about empty, single-item, and duplicate-value cases.</li>
            </ul>
          </section>

          <section className="statement-section hints-section">
            <h3>Hints</h3>
            {hints.map((hint, index) => (
              <details className="hint-card" key={hint}>
                <summary>Hint {index + 1}</summary>
                <p>{hint}</p>
              </details>
            ))}
          </section>
        </section>

        <section className="problem-workbench">
          <section className="panel editor-panel">
            <div className="editor-toolbar">
              <div>
                <p className="section-kicker">Code</p>
                <h2>Solution</h2>
              </div>
              <div className="editor-actions">
                <label className="field compact-field">
                  <span>Language</span>
                  <select value={language} onChange={(event) => setLanguage(event.target.value)}>
                    {languageOptions.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
                  </select>
                </label>
                <button className="primary-button" type="button" onClick={submitCode} disabled={isLoading || !code.trim()}>
                  {isLoading ? 'Submitting...' : 'Submit'}
                </button>
              </div>
            </div>
            <div className="monaco-frame">
              <Editor
                height="420px"
                language={language === 'cpp' ? 'cpp' : language}
                theme={theme === 'dark' ? 'vs-dark' : 'light'}
                value={code}
                onChange={(value) => setCode(value || '')}
                options={{ minimap: { enabled: false }, fontSize: 14, wordWrap: 'on' }}
              />
            </div>
            {error ? <p className="error-banner">{error}</p> : null}
          </section>

          <section className="panel result-panel">
            <SubmissionResult result={result} />
          </section>
        </section>
      </main>
    </div>
  );
}

function SubmissionResult({ result }) {
  if (!result) {
    return (
      <div className="empty-state compact-empty">
        <h3>No submission yet</h3>
        <p>Run your code to see test results, XP changes, hints, and guided feedback.</p>
      </div>
    );
  }

  const failedTest = result.testCases?.find((testCase) => !testCase.passed);
  const isCorrect = result.status === 'CORRECT';

  return (
    <div className="results-stack leetcode-results">
      <section className={`result-summary ${isCorrect ? 'accepted' : 'wrong'}`}>
        <div>
          <span className="status-pill">{isCorrect ? 'Accepted' : 'Wrong Answer'}</span>
          <h3>{result.message}</h3>
          {result.hint ? <p>{result.hint}</p> : null}
        </div>
        <div className="summary-metrics">
          <MetricCard label="Attempt" value={result.attemptNumber || 1} />
          <MetricCard label="XP" value={result.xpGained ? `+${result.xpGained}` : '0'} />
          <MetricCard label="Hint" value={result.hintLevel || 0} />
        </div>
      </section>

      <section className="result-card result-wide">
        <div className="result-card-heading">
          <h3>Test Cases</h3>
          <span>{result.testCases?.filter((testCase) => testCase.passed).length || 0}/{result.testCases?.length || 0} passed</span>
        </div>
        <div className="testcase-grid">
          {result.testCases?.map((testCase, index) => (
            <div className={`testcase-row ${testCase.passed ? 'passed' : 'failed'}`} key={`${testCase.input}-${index}`}>
              <span>{testCase.passed ? 'Passed' : 'Failed'}</span>
              <code>{testCase.input}</code>
              <div className="case-io">
                <small>Expected <strong>{testCase.expectedOutput}</strong></small>
                <small>Actual <strong>{testCase.actualOutput}</strong></small>
              </div>
            </div>
          ))}
        </div>
      </section>

      {result.aiFeedback ? (
        <div className="feedback-grid">
          <ResultCard title="What Went Wrong" items={result.aiFeedback.bugs} accent="rose" />
          {!isCorrect ? <FailureExample testCase={failedTest} /> : null}
          <ExplanationCard content={result.aiFeedback.explanation} />
          <CodeCard title="Suggested Code" code={result.aiFeedback.fixedCode} />
          <ResultCard title="Edge Cases To Try" items={result.aiFeedback.edgeCasesToTest} accent="teal" />
          <LearningResources resources={result.aiFeedback.learningResources} />
          <div className="metrics-grid">
            <MetricCard label="Time" value={result.aiFeedback.timeComplexity} />
            <MetricCard label="Space" value={result.aiFeedback.spaceComplexity} />
            <MetricCard label="Optimality" value={result.aiFeedback.optimality} />
          </div>
        </div>
      ) : null}
    </div>
  );
}

function AppTopbar({ title, theme, onToggleTheme, onLogout, searchValue, onSearchChange, searchPlaceholder }) {
  return (
    <header className="topbar glass-card workspace-topbar">
      <div className="topbar-brand">
        <p className="eyebrow">{title}</p>
        <h1 className="brand-mark workspace-brand"><span>CODE QUEST</span><em>AI</em></h1>
      </div>
      {onSearchChange ? (
        <label className="topbar-search">
          <span>Search</span>
          <input
            value={searchValue}
            onChange={(event) => onSearchChange(event.target.value)}
            placeholder={searchPlaceholder || 'Search...'}
          />
        </label>
      ) : null}
      <div className="topbar-actions">
        <ThemeToggle theme={theme} onToggle={onToggleTheme} />
        <button className="ghost-button" type="button" onClick={onLogout}>Logout</button>
      </div>
    </header>
  );
}

function XpProgress({ user }) {
  const xp = user?.xp || 0;
  const currentLevelBase = Math.pow(user?.level || 0, 2) * 10;
  const nextLevelBase = Math.pow((user?.level || 0) + 1, 2) * 10;
  const progress = Math.min(100, Math.round(((xp - currentLevelBase) / Math.max(1, nextLevelBase - currentLevelBase)) * 100));

  return (
    <section className="xp-card glass-card">
      <div>
        <span>Level {user?.level ?? 0}</span>
        <strong>{progress}% to next level</strong>
      </div>
      <div className="xp-track"><span style={{ width: `${progress}%` }} /></div>
    </section>
  );
}

function ThemeToggle({ theme, onToggle }) {
  return (
    <button className="theme-toggle" type="button" onClick={onToggle} aria-label="Toggle color theme">
      <span className="theme-toggle-track">
        <span className={theme === 'dark' ? 'theme-toggle-thumb dark' : 'theme-toggle-thumb'} />
      </span>
      <span className="theme-toggle-label">{theme === 'dark' ? 'Dark' : 'Light'}</span>
    </button>
  );
}

function ResultCard({ title, items, accent }) {
  return (
    <section className={`result-card accent-${accent || 'neutral'}`}>
      <h3>{title}</h3>
      {items && items.length > 0 ? (
        <ul className="structured-list">{items.map((item, index) => <li key={`${title}-${index}`}>{item}</li>)}</ul>
      ) : <p>No data returned.</p>}
    </section>
  );
}

function ExplanationCard({ content }) {
  const points = splitExplanation(content);

  return (
    <section className="result-card explanation-card">
      <h3>Explanation</h3>
      <div className="explanation-steps">
        {points.map((point, index) => (
          <div className="explanation-step" key={`${point}-${index}`}>
            <span>{index + 1}</span>
            <p>{point}</p>
          </div>
        ))}
      </div>
    </section>
  );
}

function CodeCard({ title, code }) {
  return (
    <section className="result-card accent-ink">
      <h3>{title}</h3>
      <pre><code>{code || 'No code returned yet.'}</code></pre>
    </section>
  );
}

function FailureExample({ testCase }) {
  if (!testCase) {
    return null;
  }

  return (
    <section className="result-card failure-example">
      <h3>Failing Example</h3>
      <div className="example-grid">
        <div>
          <span>Input</span>
          <code>{testCase.input}</code>
        </div>
        <div>
          <span>Expected</span>
          <strong>{testCase.expectedOutput}</strong>
        </div>
        <div>
          <span>Your Output</span>
          <strong>{testCase.actualOutput}</strong>
        </div>
      </div>
      <p>Your code fails this case, so trace this input first before changing the whole solution.</p>
    </section>
  );
}

function LearningResources({ resources }) {
  return (
    <section className="result-card resources-card">
      <h3>Learning Resources</h3>
      <div className="resource-list">
        {(resources && resources.length > 0 ? resources : defaultLearningResources()).map((resource, index) => {
          const parsed = parseResource(resource);
          return (
            <a href={parsed.href} target="_blank" rel="noreferrer" key={`${parsed.label}-${index}`}>
              <span>{String(index + 1).padStart(2, '0')}</span>
              <strong>{parsed.label}</strong>
            </a>
          );
        })}
      </div>
    </section>
  );
}

function MetricCard({ label, value }) {
  return (
    <div className="metric-card">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}

function splitExplanation(content) {
  if (!content || !content.trim()) {
    return ['No explanation returned yet.'];
  }

  return content
    .replace(/\s+/g, ' ')
    .split(/(?<=[.!?])\s+/)
    .map((item) => item.trim())
    .filter(Boolean)
    .slice(0, 6);
}

function parseResource(resource) {
  const match = String(resource).match(/(https?:\/\/\S+)/);
  const href = match ? match[1].replace(/[).,]+$/, '') : '#';
  const label = String(resource)
    .replace(/https?:\/\/\S+/, '')
    .replace(/[:\-]+$/, '')
    .trim() || href;
  return { href, label };
}

function defaultLearningResources() {
  return [
    'Binary search patterns: https://leetcode.com/explore/learn/card/binary-search/',
    'Algorithm complexity guide: https://www.bigocheatsheet.com/',
    'DSA roadmap: https://neetcode.io/roadmap'
  ];
}

function parseProblemExamples(testCasesJson) {
  if (!testCasesJson) {
    return [{ input: 'No examples configured yet.', expectedOutput: '-' }];
  }

  try {
    const parsed = JSON.parse(testCasesJson);
    if (!Array.isArray(parsed) || parsed.length === 0) {
      return [{ input: 'No examples configured yet.', expectedOutput: '-' }];
    }

    return parsed.slice(0, 3).map((testCase) => ({
      input: testCase.input || '-',
      expectedOutput: testCase.expectedOutput || '-'
    }));
  } catch {
    return [{ input: 'Could not parse examples.', expectedOutput: '-' }];
  }
}

function problemHints(problem) {
  const title = (problem?.title || '').toLowerCase();

  if (title.includes('two sum')) {
    return [
      'For each number, ask what value would complete the target.',
      'Use a map from number to index so each lookup is O(1). Store the current number only after checking its complement.'
    ];
  }

  if (title.includes('binary search')) {
    return [
      'Keep two boundaries, left and right, and only search while left <= right.',
      'Compare nums[mid] with target, then move exactly one boundary: left = mid + 1 or right = mid - 1.'
    ];
  }

  if (title.includes('reverse linked list')) {
    return [
      'Track previous, current, and next nodes so you do not lose the rest of the list.',
      'On each step, point current.next backward, then move previous and current forward.'
    ];
  }

  return [
    'Trace the smallest valid input by hand before coding the general case.',
    'Look for the state that changes each step, then update it in one consistent direction.'
  ];
}

async function request(path, options = {}) {
  const isFormData = options.body instanceof FormData;
  const headers = {
    ...(isFormData ? {} : { 'Content-Type': 'application/json' }),
    ...(options.headers || {})
  };

  if (options.token) {
    headers.Authorization = `Bearer ${options.token}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
    credentials: 'include'
  });

  if (!response.ok) {
    const error = await response.json().catch(() => null);
    throw new Error(error?.message || `Request failed with status ${response.status}`);
  }

  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

async function refreshSession() {
  return request('/auth/refresh', {
    method: 'POST',
    body: JSON.stringify({})
  });
}

function passwordStrength(password) {
  let score = 0;
  if (password.length >= 8) score += 1;
  if (/[A-Z]/.test(password) && /[a-z]/.test(password)) score += 1;
  if (/\d/.test(password)) score += 1;
  if (/[^A-Za-z0-9]/.test(password)) score += 1;

  const labels = ['Use 8+ characters', 'Weak password', 'Fair password', 'Good password', 'Strong password'];
  return { score, label: labels[score] };
}

function readStoredUser() {
  try {
    const userJson = window.localStorage.getItem('auth-user');
    return userJson ? JSON.parse(userJson) : null;
  } catch {
    return null;
  }
}

function clearStoredSession() {
  window.localStorage.removeItem('auth-token');
  window.localStorage.removeItem('auth-user');
}

function toggleTheme(setTheme) {
  setTheme((currentTheme) => currentTheme === 'light' ? 'dark' : 'light');
}

export default App;
