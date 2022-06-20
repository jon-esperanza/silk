<p align="center">
    <a href="https://trino.io/"><img alt="Trino Logo" src=".github/silk-logo.png" /></a>
</p>
<p align="center">
    <b>Silk is an adaptive distributed stream processing engine.</b>
</p>
<p align="center">
   <a href="https://github.com/jonesperanza/silk/releases">
       <img src="https://img.shields.io/github/v/release/jonesperanza/silk" alt="Silk latest release" />
   </a>
   <a href="https://github.com/jonesperanza/silk/actions/workflows/ci.yml">
       <img alt="Silk build status" src="https://img.shields.io/github/workflow/status/jonesperanza/silk/ci">
   </a>
   <a href="https://github.com/jonesperanza/silk/blob/master/LICENSE.md">
       <img src="https://img.shields.io/github/license/jonesperanza/silk" alt="Silk license" />
   </a>
</p>

## About Silk
Consuming data through event streaming platforms is very advantageous, when done right.
Executing multiple jobs for multiple streams of data can be costly and take way too much time to complete.

Silk offers reliable job execution for stream data processing. Users can easily embed Silk into their stream processing pipelines.

With an adaptive intuition, our engine is able to understand your job executions better than you do! This helps users dynamically schedule their jobs based on important situational metrics like data size and type.

Silk makes it easy to model stream data, schedule multiple jobs, and scale self-improving stream processes.

### Built With
* [Scala](https://www.scala-lang.org/)

## Roadmap
- [ ] Set up repo
- [ ] Kafka integration with data modeling
  - [ ] Merchant & Job Objects
  - [ ] Centralized Single Instance
  - [ ] Easy Kafka Setup
  - [ ] Research how jobs can be user-defined and used by engine
- [ ] Distributed System
    - [ ] MapReduce-esque distribution of stream processing jobs
    - [ ] Master Distribution Center (MDC) scheduling node
    - [ ] Worker nodes
    - [ ] Fault-tolerant state store pattern & process
- [ ] Monitoring and data collection
  - [ ] Efficient and relevant monitoring
  - [ ] Collect useful data
- [ ] Adaptive model
  - [ ] Post-job execution analysis
  - [ ] Effective storage and retrieval of adaptive model
  - [ ] Efficient performance testing
- [ ] GUI

