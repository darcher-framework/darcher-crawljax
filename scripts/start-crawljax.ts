import * as prompts from "prompts";
import * as path from "path";
import * as fs from "fs";
import parseDuration from "parse-duration";
import * as child_process from "child_process";
import {Logger, sleep} from "@darcher/helpers";
import {log} from "util";

class Worker {
    public static stdoutFile = path.join(__dirname, "stdout.log");
    public static stderrFile = path.join(__dirname, "stderr.log");
    public static statusFile = path.join(__dirname, "status.log");
    public static coverageDir = path.join(__dirname, "coverage", "client");

    private process: child_process.ChildProcess | null;
    private stdoutStream: NodeJS.WritableStream;
    private stderrStream: NodeJS.WritableStream;

    private started = false;

    constructor(
        private readonly logger: Logger,
        private readonly chromeDebuggerAddress: string,
        private readonly metamaskUrl: string,
        private readonly metamaskPassword: string,
        private readonly subject: string,
    ) {
        // if (fs.existsSync(Worker.coverageDir)) {
        //     fs.rmdirSync(Worker.coverageDir, {recursive: true});
        // }
    }

    public async start() {
        if (!this.started) {
            this.logger.info("Initial setting up...");
            Worker.setup();
            this.stdoutStream = fs.createWriteStream(Worker.stdoutFile, {flags: "a"});
            this.stderrStream = fs.createWriteStream(Worker.stderrFile, {flags: "a"});
            await new Promise<void>((resolve, reject) => {
                const p = child_process.spawn("mvn",
                    ["install", "-DskipTests"], {
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
    }

    public restart() {
        // create stdout/stderr file stream
        this.stdoutStream = fs.createWriteStream(Worker.stdoutFile, {flags: "a"});
        this.stderrStream = fs.createWriteStream(Worker.stderrFile, {flags: "a"});
        // start process
        this.process = child_process.spawn(
            "mvn",
            ["exec:java", "-pl", "examples"], {
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

    public stop() {
        if (!this.process) {
            return;
        }
        child_process.spawnSync("kill", ["-INT", this.process.pid.toString()]); // kill
        Worker.cleanProcess();
        this.logger.info("Crawljax process stopped");
    }


    private static setup() {
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

    private static cleanProcess() {
        // child_process.spawnSync("pkill", ["-INT", "chromedriver"]); // kill chrome driver
        // child_process.spawnSync("pkill", ["-INT", "Google Chrome"]); // kill Google Chrome
        try {
            child_process.execSync("lsof -ti:1237 | xargs kill"); // kill the websocket server on port 1237
            // eslint-disable-next-line no-empty
        } catch (ignored) {
        }
    }
}

export async function startCrawljax(logger: Logger, chromeDebuggerAddress: string, metamaskUrl: string, metamaskPassword: string, mainClass: string, timeBudget: number, logDir?: string) {
    if (logDir) {
        Worker.stdoutFile = path.join(logDir, "crawljax.stdout.log");
        Worker.stderrFile = path.join(logDir, "crawljax.stderr.log");
        Worker.statusFile = path.join(logDir, "crawljax.status.log");
        Worker.coverageDir = path.join(logDir, "coverage", "client");
    }
    // eslint-disable-next-line no-async-promise-executor
    return new Promise<void>(async resolve => {
        type CrawljaxStatus =
            "Maximum time passed"
            | "Maximum states passed"
            | "Exhausted"
            | "Errored"
            | "Stopped manually";

        let shouldContinue = true;
        const subprocess: Worker = new Worker(logger, chromeDebuggerAddress, metamaskUrl, metamaskPassword, mainClass);
        await subprocess.start();

        // watch status of crawljax, since crawljax cannot exit by itself
        const checkCrawljaxStatue = (): CrawljaxStatus | null => {
            if (!fs.existsSync(Worker.statusFile)) {
                return null;
            }
            return fs.readFileSync(Worker.statusFile, {encoding: "utf-8"}) as CrawljaxStatus;
        };
        const interval = setInterval(async () => {
            const status = checkCrawljaxStatue();
            if (!status) {
                // file not exist
                return;
            }
            logger.info("Crawljax status updated", {status: status});
            switch (status) {
                case "Errored":
                case "Stopped manually":
                    // should stop the process ahead
                    await subprocess.stop();
                    break;
                case "Maximum time passed":
                case "Maximum states passed":
                case "Exhausted":
                    // should stop the process ahead
                    await subprocess.stop();
                    if (shouldContinue) {
                        if (fs.existsSync(Worker.statusFile)) {
                            fs.unlinkSync(Worker.statusFile);
                        }
                        await subprocess.start();
                    }
                    break;
                default:
                    logger.warn("Unknown crawljax status", {status});
            }
        }, 500);
        setTimeout(async () => {
            shouldContinue = false;
            subprocess.stop();
            logger.info("Crawljax timeout", {timeBudget: timeBudget + "s"});
            clearInterval(interval);
            await sleep(1000);
            resolve();
        }, timeBudget * 1000);
    });
}

if (require.main === module) {
    (async () => {
        /**
         *
         * @param name
         * @return path to main class
         */
        const findMainClass = (name: string): string | null => {
            const examplesDir = path.join(__dirname, "..", "examples", "src", "main", "java", "com", "darcher",
            "crawljax", "experiments");
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

        const response0 = await prompts({
            type: "text",
            name: "mainClass",
            message: "What is the main class?",
            validate: (prev: string) => findMainClass(prev) ? true : `Main class '${prev}' not found`,
            format: prev => findMainClass(prev),
        });

        const parseTimeBudget = (budget: string): number | null => {
            return parseDuration(budget, "s");
        };

        const response1 = await prompts({
            type: "text",
            name: "timeBudget",
            message: "What is the time budget?",
            validate: prev => typeof parseTimeBudget(prev) === "number",
            format: prev => parseTimeBudget(prev) as number,
        });

        const logger = new Logger("Crawljax", "info");
        logger.info("Starting crawljax...", {
            subject: path.basename(response0.mainClass),
            timeBudget: response1.timeBudget + "s",
        });

        await startCrawljax(logger, "localhost:9222", "chrome-extension://cihfjnmdkdfgilhaaiepgmdgglglhjbh/home.html", "12345678", response0.mainClass, response1.timeBudget);
    })();
}
