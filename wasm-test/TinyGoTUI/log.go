package tui

import (
	"fmt"

	"github.com/wasmcraft/bindings/monitor"
)

type LogLevel int

const (
	LogInfo LogLevel = iota
	LogWarning
	LogError
	LogDebug
	LogSuccess
)

type LogEntry struct {
	Level   LogLevel
	Message string
}

type Log struct {
	Entries         []LogEntry
	ShowTimestamps  bool
	ShowLineNumbers bool
	Scale           int
	MaxLines        int
}

func NewLog() *Log {
	return &Log{
		Entries:         []LogEntry{},
		ShowTimestamps:  false,
		ShowLineNumbers: false,
		Scale:           1,
		MaxLines:        100,
	}
}

func (l *Log) AddEntry(level LogLevel, message string) {
	l.Entries = append(l.Entries, LogEntry{Level: level, Message: message})
	if len(l.Entries) > l.MaxLines {
		l.Entries = l.Entries[1:]
	}
}

func (l *Log) Info(message string) {
	l.AddEntry(LogInfo, message)
}

func (l *Log) Warning(message string) {
	l.AddEntry(LogWarning, message)
}

func (l *Log) Error(message string) {
	l.AddEntry(LogError, message)
}

func (l *Log) Debug(message string) {
	l.AddEntry(LogDebug, message)
}

func (l *Log) Success(message string) {
	l.AddEntry(LogSuccess, message)
}

func (l *Log) Clear() {
	l.Entries = []LogEntry{}
}

func (l *Log) SetShowTimestamps(show bool) {
	l.ShowTimestamps = show
}

func (l *Log) SetShowLineNumbers(show bool) {
	l.ShowLineNumbers = show
}

func (l *Log) SetScale(scale int) {
	if scale < 1 {
		scale = 1
	}
	l.Scale = scale
}

func (l *Log) getLevelColor(level LogLevel) Color {
	switch level {
	case LogInfo:
		return Cyan
	case LogWarning:
		return Yellow
	case LogError:
		return Red
	case LogDebug:
		return Magenta
	case LogSuccess:
		return Green
	default:
		return White
	}
}

func (l *Log) getLevelPrefix(level LogLevel) string {
	switch level {
	case LogInfo:
		return "[INFO]"
	case LogWarning:
		return "[WARN]"
	case LogError:
		return "[ERROR]"
	case LogDebug:
		return "[DEBUG]"
	case LogSuccess:
		return "[OK]"
	default:
		return "[LOG]"
	}
}

func (l *Log) MinSize(monitorID string) (width, height int) {
	if len(l.Entries) == 0 {
		return 0, 0
	}

	maxWidth := 0
	totalHeight := 0

	for i, entry := range l.Entries {
		line := l.formatLine(i, entry)
		w, h := monitor.MeasureText(monitorID, line, l.Scale)
		if w > maxWidth {
			maxWidth = w
		}
		totalHeight += h
	}

	return maxWidth, totalHeight
}

func (l *Log) formatLine(index int, entry LogEntry) string {
	line := ""

	if l.ShowLineNumbers {
		line += fmt.Sprintf("%3d ", index+1)
	}

	line += l.getLevelPrefix(entry.Level) + " " + entry.Message

	return line
}

func (l *Log) Render(monitorID string, region Rect) {
	if len(l.Entries) == 0 {
		return
	}

	_, lineHeight := monitor.MeasureText(monitorID, "A", l.Scale)

	maxVisibleLines := region.Height / lineHeight
	if maxVisibleLines <= 0 {
		return
	}

	startIndex := 0
	if len(l.Entries) > maxVisibleLines {
		startIndex = len(l.Entries) - maxVisibleLines
	}

	y := region.Y

	for i := startIndex; i < len(l.Entries); i++ {
		entry := l.Entries[i]

		if y+lineHeight > region.Y+region.Height {
			break
		}

		x := region.X

		if l.ShowLineNumbers {
			lineNum := fmt.Sprintf("%3d ", i+1)
			numWidth, _ := monitor.MeasureText(monitorID, lineNum, l.Scale)
			monitor.DrawText(
				monitorID,
				x, y,
				lineNum,
				Gray.R, Gray.G, Gray.B,
				0, 0, 0,
				l.Scale,
			)
			x += numWidth
		}

		prefix := l.getLevelPrefix(entry.Level) + " "
		levelColor := l.getLevelColor(entry.Level)
		prefixWidth, _ := monitor.MeasureText(monitorID, prefix, l.Scale)

		monitor.DrawText(
			monitorID,
			x, y,
			prefix,
			levelColor.R, levelColor.G, levelColor.B,
			0, 0, 0,
			l.Scale,
		)
		x += prefixWidth

		monitor.DrawText(
			monitorID,
			x, y,
			entry.Message,
			White.R, White.G, White.B,
			0, 0, 0,
			l.Scale,
		)

		y += lineHeight
	}
}
