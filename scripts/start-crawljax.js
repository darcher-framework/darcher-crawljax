"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.startCrawljax = void 0;
const prompts = require("prompts");
const path = require("path");
const fs = require("fs");
const parse_duration_1 = require("parse-duration");
const child_process = require("child_process");
const helpers_1 = require("@darcher/helpers");
class Worker {
    constructor(logger, chromeDebuggerAddress, metamaskUrl, metamaskPassword, subject) {
        this.logger = logger;
        this.chromeDebuggerAddress = chromeDebuggerAddress;
        this.metamaskUrl = metamaskUrl;
        this.metamaskPassword = metamaskPassword;
        this.subject = subject;
        this.started = false;
        // if (fs.existsSync(Worker.coverageDir)) {
        //     fs.rmdirSync(Worker.coverageDir, {recursive: true});
        // }
    }
    start() {
        return __awaiter(this, void 0, void 0, function* () {
            if (!this.started) {
                this.logger.info("Initial setting up...");
                Worker.setup();
                this.stdoutStream = fs.createWriteStream(Worker.stdoutFile, { flags: "a" });
                this.stderrStream = fs.createWriteStream(Worker.stderrFile, { flags: "a" });
                yield new Promise((resolve, reject) => {
                    const p = child_process.spawn("mvn", ["install", "-DskipTests"], {
                        cwd: path.join(__dirname, ".."),
                        stdio: ["inherit", "pipe", "pipe"],
                    });
                    // pipe stdout/stderr to file
                    p.stdout.pipe(this.stdoutStream);
                    p.stderr.pipe(this.stderrStream);
                    this.logger.info("Compiling crawljax...");
                    p.on("exit", () => resolve());
                    p.on("error", err => reject(err));
                });
                this.logger.info("Compiling crawljax...done");
                this.started = true;
            }
            this.restart();
            this.logger.info("Crawljax process started");
        });
    }
    restart() {
        // create stdout/stderr file stream
        this.stdoutStream = fs.createWriteStream(Worker.stdoutFile, { flags: "a" });
        this.stderrStream = fs.createWriteStream(Worker.stderrFile, { flags: "a" });
        // start process
        this.process = child_process.spawn("mvn", ["exec:java", "-pl", "examples"], {
            cwd: path.join(__dirname, ".."),
            stdio: ["inherit", "pipe", "pipe"],
            env: Object.assign(process.env, {
                COVERAGE_DIR: Worker.coverageDir,
                STATUS_LOG_PATH: Worker.statusFile,
                CHROME_DEBUGGER_ADDRESS: this.chromeDebuggerAddress,
                SUBJECT: this.subject,
                METAMASK_URL: this.metamaskUrl,
                METAMASK_PASSWORD: this.metamaskPassword,
            }),
        });
        this.process.on("exit", () => {
            // if the process exit by itself, we set this.process = null
            // this.process = null;
        });
        this.process.on("error", err => {
            this.logger.warn(err.message);
        });
        // pipe stdout/stderr to file
        this.process.stdout.pipe(this.stdoutStream);
        this.process.stderr.pipe(this.stderrStream);
    }
    stop() {
        if (!this.process) {
            return;
        }
        child_process.spawnSync("kill", ["-INT", this.process.pid.toString()]); // kill
        Worker.cleanProcess();
        this.logger.info("Crawljax process stopped");
    }
    static setup() {
        if (fs.existsSync(Worker.stdoutFile)) {
            fs.unlinkSync(Worker.stdoutFile);
        }
        if (fs.existsSync(Worker.stderrFile)) {
            fs.unlinkSync(Worker.stderrFile);
        }
        if (fs.existsSync(Worker.statusFile)) {
            fs.unlinkSync(Worker.statusFile);
        }
        Worker.cleanProcess();
    }
    static cleanProcess() {
        // child_process.spawnSync("pkill", ["-INT", "chromedriver"]); // kill chrome driver
        // child_process.spawnSync("pkill", ["-INT", "Google Chrome"]); // kill Google Chrome
        try {
            child_process.execSync("lsof -ti:1237 | xargs kill"); // kill the websocket server on port 1237
            // eslint-disable-next-line no-empty
        }
        catch (ignored) {
        }
    }
}
Worker.stdoutFile = path.join(__dirname, "stdout.log");
Worker.stderrFile = path.join(__dirname, "stderr.log");
Worker.statusFile = path.join(__dirname, "status.log");
Worker.coverageDir = path.join(__dirname, "coverage", "client");
function startCrawljax(logger, chromeDebuggerAddress, metamaskUrl, metamaskPassword, mainClass, timeBudget, logDir) {
    return __awaiter(this, void 0, void 0, function* () {
        if (logDir) {
            Worker.stdoutFile = path.join(logDir, "crawljax.stdout.log");
            Worker.stderrFile = path.join(logDir, "crawljax.stderr.log");
            Worker.statusFile = path.join(logDir, "crawljax.status.log");
            Worker.coverageDir = path.join(logDir, "coverage", "client");
        }
        // eslint-disable-next-line no-async-promise-executor
        return new Promise((resolve) => __awaiter(this, void 0, void 0, function* () {
            let shouldContinue = true;
            const subprocess = new Worker(logger, chromeDebuggerAddress, metamaskUrl, metamaskPassword, mainClass);
            yield subprocess.start();
            // watch status of crawljax, since crawljax cannot exit by itself
            const checkCrawljaxStatue = () => {
                if (!fs.existsSync(Worker.statusFile)) {
                    return null;
                }
                return fs.readFileSync(Worker.statusFile, { encoding: "utf-8" });
            };
            const interval = setInterval(() => __awaiter(this, void 0, void 0, function* () {
                const status = checkCrawljaxStatue();
                if (!status) {
                    // file not exist
                    return;
                }
                logger.info("Crawljax status updated", { status: status });
                switch (status) {
                    case "Errored":
                    case "Stopped manually":
                        // should stop the process ahead
                        yield subprocess.stop();
                        break;
                    case "Maximum time passed":
                    case "Maximum states passed":
                    case "Exhausted":
                        // should stop the process ahead
                        yield subprocess.stop();
                        if (shouldContinue) {
                            if (fs.existsSync(Worker.statusFile)) {
                                fs.unlinkSync(Worker.statusFile);
                            }
                            yield subprocess.start();
                        }
                        break;
                    default:
                        logger.warn("Unknown crawljax status", { status });
                }
            }), 500);
            setTimeout(() => __awaiter(this, void 0, void 0, function* () {
                shouldContinue = false;
                subprocess.stop();
                logger.info("Crawljax timeout", { timeBudget: timeBudget + "s" });
                clearInterval(interval);
                yield helpers_1.sleep(1000);
                resolve();
            }), timeBudget * 1000);
        }));
    });
}
exports.startCrawljax = startCrawljax;
if (require.main === module) {
    (() => __awaiter(void 0, void 0, void 0, function* () {
        /**
         *
         * @param name
         * @return path to main class
         */
        const findMainClass = (name) => {
            const examplesDir = path.join(__dirname, "..", "examples", "src", "main", "java", "com", "darcher", "crawljax", "experiments");
            for (const file of fs.readdirSync(examplesDir)) {
                if (path.extname(file) !== ".java") {
                    continue;
                }
                const basename = path.basename(file).slice(0, path.basename(file).length - 5);
                if (basename.toLowerCase().trim() === name.toLowerCase().trim() ||
                    basename.toLowerCase().trim() === name.toLowerCase().trim() + "experiment" ||
                    basename.toLowerCase().trim() === name.toLowerCase().trim() + "example") {
                    return basename;
                }
            }
            return null;
        };
        const response0 = yield prompts({
            type: "text",
            name: "mainClass",
            message: "What is the main class?",
            validate: (prev) => findMainClass(prev) ? true : `Main class '${prev}' not found`,
            format: prev => findMainClass(prev),
        });
        const parseTimeBudget = (budget) => {
            return parse_duration_1.default(budget, "s");
        };
        const response1 = yield prompts({
            type: "text",
            name: "timeBudget",
            message: "What is the time budget?",
            validate: prev => typeof parseTimeBudget(prev) === "number",
            format: prev => parseTimeBudget(prev),
        });
        const logger = new helpers_1.Logger("Crawljax", "info");
        logger.info("Starting crawljax...", {
            subject: path.basename(response0.mainClass),
            timeBudget: response1.timeBudget + "s",
        });
        yield startCrawljax(logger, "localhost:9222", "chrome-extension://cihfjnmdkdfgilhaaiepgmdgglglhjbh/home.html", "12345678", response0.mainClass, response1.timeBudget);
    }))();
}
//# sourceMappingURL=start-crawljax.js.map